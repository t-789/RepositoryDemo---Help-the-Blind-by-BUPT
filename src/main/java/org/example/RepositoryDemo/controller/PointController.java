// PointController.java
package org.example.RepositoryDemo.controller;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.dto.PointRequest;
import org.example.RepositoryDemo.entity.Point;
import org.example.RepositoryDemo.entity.User;
import org.example.RepositoryDemo.service.PointService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/points")
@Validated
@Setter
public class PointController {
    
    private static final Logger logger = LogManager.getLogger(PointController.class);

    private PointService pointService;

    private UserRepository userRepository;
    
    // 保存点位
    @PostMapping(value="/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> savePoint(@Valid @ModelAttribute PointRequest pointRequest, Authentication authentication) {
        try {
            // 获取当前用户ID
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("还没有登录");
            }
            String imagePath = null;
            if (pointRequest.getImage_description() != null && !pointRequest.getImage_description().isEmpty()){
                imagePath = saveImageFile(pointRequest.getImage_description());
            }
            int pointId = pointService.savePoint(user.id, pointRequest.getX(), pointRequest.getY(),pointRequest.getLevel(), pointRequest.getType(), pointRequest.getDescription(), imagePath);
            if (pointId > 0) {
                return ResponseEntity.ok(Map.of("id", pointId, "message", "点位保存成功"));
            } else if (pointId == -1) {
                return ResponseEntity.badRequest().body("点位已存在");
            } else {
                return ResponseEntity.badRequest().body("点位保存失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("参数错误: " + e.getMessage());
        }
    }
    
    // 获取所有活跃点位
    @GetMapping("/active")
    public ResponseEntity<?> getActivePoints() {
        List<Point> points = pointService.getAllActivePoints();
        return ResponseEntity.ok(points);
    }
    
    // 提议删除点位
    @PostMapping("/{pointId}/propose-delete")
    public ResponseEntity<?> proposeDeletePoint(@Min(value = 1, message = "点位ID必须大于0") @PathVariable int pointId, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("还没有登录");
            }
            
            int status = pointService.proposeDeletePoint(pointId, user.id);
            switch (status){
                case 1 -> {
                    return ResponseEntity.ok("提议删除成功");
                }
                case -1 -> {
                    return ResponseEntity.badRequest().body("提议删除失败，可能是因为点位不存在");
                }
                case -2 -> {
                    return ResponseEntity.badRequest().body("-2: 用户已提议过，不允许重复提议");
                }
                case -4 -> {
                    return ResponseEntity.badRequest().body("-4: 点位已删除");
                }
                default -> {
                    return ResponseEntity.badRequest().body(status + "操作失败，错误原因未知，请联系管理员。");
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }
    
    // 管理员删除点位
    @DeleteMapping("/{pointId}")
    public ResponseEntity<?> adminDeletePoint(@Min(value = 1, message = "点位ID必须大于0") @PathVariable int pointId, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null || user.type != 2) {
                return ResponseEntity.badRequest().body("权限不足");
            }
            
            boolean success = pointService.adminDeletePoint(pointId);
            if (success) {
                return ResponseEntity.ok("点位删除成功");
            } else {
                return ResponseEntity.badRequest().body("点位删除失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }
    
    // 管理员恢复点位
    @PostMapping("/{pointId}/restore")
    public ResponseEntity<?> adminRestorePoint(@Min(value = 1, message = "点位ID必须大于0") @PathVariable int pointId, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null || user.type != 2) {
                return ResponseEntity.badRequest().body("权限不足");
            }
            
            boolean success = pointService.adminRestorePoint(pointId);
            if (success) {
                return ResponseEntity.ok("点位恢复成功");
            } else {
                return ResponseEntity.badRequest().body("点位恢复失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }
    
    // 获取删除阈值
    @GetMapping("/threshold")
    public ResponseEntity<?> getDeleteThreshold() {
        int threshold = pointService.getDeleteThreshold();
        return ResponseEntity.ok(Map.of("threshold", threshold));
    }
    
    // 设置删除阈值（仅管理员）
    @PostMapping("/threshold")
    public ResponseEntity<?> setDeleteThreshold(@RequestBody Map<String, Integer> payload, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null || user.type != 2) {
                return ResponseEntity.badRequest().body("权限不足");
            }
            
            Integer threshold = payload.get("threshold");
            if (threshold == null) {
                return ResponseEntity.badRequest().body("阈值不能为空");
            }
            
            if (threshold <= 0) {
                return ResponseEntity.badRequest().body("阈值必须大于0");
            }
            
            boolean success = pointService.setDeleteThreshold(threshold);
            if (success) {
                return ResponseEntity.ok("阈值设置成功");
            } else {
                return ResponseEntity.badRequest().body("阈值设置失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("参数错误: " + e.getMessage());
        }
    }
    
    // 确认点位存在
    @PostMapping("/{pointId}/confirm")
    public ResponseEntity<?> confirmPoint(@Min(value = 1, message = "点位ID必须大于0") @PathVariable int pointId, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("还没有登录");
            }
            
            // 检查点位是否由当前用户创建（如果是，则不能确认自己的点位）
            Point point = pointService.getPointById(pointId);
            if (point == null) {
                return ResponseEntity.badRequest().body("点位不存在");
            }
            
            if (Objects.equals(point.userId, user.id)) {
                return ResponseEntity.badRequest().body("不能确认自己创建的点位");
            }
            
            int status = pointService.confirmPoint(pointId, user.id);
            switch (status) {
                case 1 -> {
                    return ResponseEntity.ok("确认点位成功");
                }
                case -1 -> {
                    return ResponseEntity.badRequest().body("确认点位失败，可能是因为点位不存在");
                }
                case -2 -> {
                    return ResponseEntity.badRequest().body("用户已确认过该点位，不允许重复确认");
                }
                case -4 -> {
                    return ResponseEntity.badRequest().body("点位已删除");
                }
                default -> {
                    return ResponseEntity.badRequest().body(status + "操作失败，错误原因未知，请联系管理员。");
                }
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }
    
    // 获取所有点位（包括已删除的）- 仅管理员可用
    @GetMapping("/all")
    public ResponseEntity<?> getAllPoints(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null || user.type != 2) {
                return ResponseEntity.badRequest().body("权限不足");
            }

            List<Point> points = pointService.getAllPoints(); // 获取所有点位
            
            return ResponseEntity.ok(points);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }

    // 保存类型映射（仅管理员）
    @PostMapping("/type-map")
    public ResponseEntity<?> saveTypeMap(@RequestBody Map<String, Object> payload, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null || user.type != 2) {
                return ResponseEntity.badRequest().body("权限不足");
            }
            
            Integer typeId = (Integer) payload.get("typeId");
            String typeName = (String) payload.get("typeName");
            
            if (typeId == null || typeName == null || typeName.isEmpty()) {
                return ResponseEntity.badRequest().body("类型ID和类型名称不能为空");
            }
            
            boolean success = pointService.saveTypeMap(typeId, typeName);
            if (success) {
                return ResponseEntity.ok("类型映射保存成功");
            } else {
                return ResponseEntity.badRequest().body("类型映射保存失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("参数错误: " + e.getMessage());
        }
    }
    
    // 获取所有类型映射
    @GetMapping("/type-map")
    public ResponseEntity<?> getAllTypeMaps() {
        try {
            java.util.Map<Integer, String> typeMaps = pointService.getAllTypeMaps();
            return ResponseEntity.ok(typeMaps);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }

    // 根据距离获取点位
    @GetMapping("/distance")
    public ResponseEntity<?> getPointsByDistance(@RequestParam double x, @RequestParam double y, @RequestParam double distance) {
        if (x < 0 || y < 0 || distance <= 0) {
            if (x <= 0){
                return ResponseEntity.badRequest().body("x坐标不能小于0");
            } else if (y <= 0) {
                return ResponseEntity.badRequest().body("y坐标不能小于0");
            }
            return ResponseEntity.badRequest().body("距离不能小于等于0");
        }
        try {
            List<Point> points = pointService.getPointsByDistance(x, y, distance);
            return ResponseEntity.ok(points);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }

    // 保存头像文件的辅助方法
    private String saveImageFile(MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return null;
            }

            // 检查文件类型
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                return null;
            }

            // 检查文件大小（限制为10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return null;
            }

            // 创建头像存储目录
            String uploadDir = "./external/static/descriptions/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                if(!dir.mkdirs()){
                    logger.fatal("创建点位图片描述存储目录失败");
                    return null;
                }
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                // 验证扩展名是否合法
                if (!extension.equals(".jpg") && !extension.equals(".jpeg") && !extension.equals(".png")) {
                    return null;
                }
            }
            String fileName = UUID.randomUUID() + extension;
            Path path = Paths.get(uploadDir);
            Path filePath = path.resolve(fileName).normalize();
            
            // 验证文件路径是否在允许的目录内
            Path allowedDir = path.toAbsolutePath().normalize();
            if (!filePath.toAbsolutePath().normalize().startsWith(allowedDir)) {
                logger.error("非法文件路径访问尝试");
                return null;
            }

            // 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回头像访问路径
            return "/description/" + fileName;
        } catch (IOException e) {
            logger.error("保存点位图片描述文件失败: {}", e.getMessage());
            return null;
        }
    }
}