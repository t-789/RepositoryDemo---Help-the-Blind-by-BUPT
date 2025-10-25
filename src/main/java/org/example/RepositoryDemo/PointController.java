// PointController.java
package org.example.RepositoryDemo;

import org.example.RepositoryDemo.dto.PointRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/points")
@Validated
public class PointController {
    
    @Autowired
    private PointService pointService;
    
    @Autowired
    private UserRepository userRepository;
    
    // 保存点位
    @PostMapping("/save")
    public ResponseEntity<?> savePoint(@Valid @RequestBody PointRequest pointRequest, Authentication authentication) {
        try {
            // 获取当前用户ID
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("还没有登录");
            }
            
            int pointId = pointService.savePoint(user.id, pointRequest.getX(), pointRequest.getY(),pointRequest.getLevel(), pointRequest.getType(), pointRequest.getDescription());
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
            
            if (point.userId == user.id) {
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
}