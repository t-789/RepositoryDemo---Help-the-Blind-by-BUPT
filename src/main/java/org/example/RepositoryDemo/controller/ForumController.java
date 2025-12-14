package org.example.RepositoryDemo.controller;

import jakarta.validation.constraints.Max;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.ForumRepository;
import org.example.RepositoryDemo.dto.ForumAndPointRequest;
import org.example.RepositoryDemo.dto.ForumPictureRequest;
import org.example.RepositoryDemo.service.ForumService;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.dto.ForumRequest;
import org.example.RepositoryDemo.entity.Forum;
import org.example.RepositoryDemo.entity.User;
import org.example.RepositoryDemo.service.PointService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/api/forum")
@Validated
public class ForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private PointService pointService;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LogManager.getLogger(ForumController.class);
    @Autowired
    private ForumRepository forumRepository;

    // 创建论坛帖子
    @PostMapping("/create")
    public ResponseEntity<?> createForum(@Valid @RequestBody ForumRequest forumRequest, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(403).body("请先登录");
            }

            int result = forumService.createForum(user.id, forumRequest.getTitle(), forumRequest.getContent(), 1);
            if (result > 0) {
                return ResponseEntity.ok("帖子创建成功");
            } else {
                return ResponseEntity.badRequest().body("帖子创建失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建帖子失败: " + e.getMessage());
        }
    }

    // 获取所有论坛帖子
    @GetMapping("/all")
    public ResponseEntity<?> getAllForums(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(403).body("请先登录");
            }

            List<Forum> forums = forumService.getAllForumsWithUserStatus(user.id);
            return ResponseEntity.ok(forums);
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("获取论坛帖子失败: " + e.getMessage());
        }
    }

    // 根据用户ID获取论坛帖子（不需要管理员权限）
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getForumsByUserId(@Min(value = 1, message = "用户ID必须大于0") @PathVariable int userId, Authentication authentication) {
        try {
            // 检查用户是否已认证
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(403).body("请先登录");
            }

            List<Forum> forums = forumService.getForumsByUserId(userId);
            return ResponseEntity.ok(forums);
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("获取用户论坛帖子失败: " + e.getMessage());
        }
    }

    // 根据帖子ID获取特定帖子
    @GetMapping("/{forumId}")
    public ResponseEntity<?> getForumById(@Min(value = 1, message = "帖子ID必须大于0") @PathVariable int forumId, Authentication authentication) {
        try {
            // 检查用户是否已认证
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(403).body("请先登录");
            }

            Forum forum = forumService.getForumById(forumId);
            if (forum.getId() != null) {
                return ResponseEntity.ok(forum);
            } else {
                return ResponseEntity.badRequest().body("帖子不存在");
            }
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("获取帖子失败: " + e.getMessage());
        }
    }

    // 管理员删除论坛帖子
    @DeleteMapping("/{forumId}")
    public ResponseEntity<?> deleteForum(@Min(value = 1, message = "帖子ID必须大于0") @PathVariable int forumId, Authentication authentication) {
        try {
            // 检查用户是否已认证
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null || user.type != 2) {
                return ResponseEntity.badRequest().body("权限不足");
            }

            forumService.deleteForum(forumId);
            return ResponseEntity.ok("帖子删除成功");
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("删除帖子失败: " + e.getMessage());
        }
    }
    
    // 点赞帖子 /api/forum/{forumId}/like
    @PostMapping("/{forumId}/like")
    public ResponseEntity<?> likeForum(@Min(value = 1, message = "帖子ID必须大于0") @PathVariable int forumId, Authentication authentication) {
        try {
            // 检查用户是否已认证
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            boolean result = forumService.likeForum(forumId, user.id);
            if (result) {
                return ResponseEntity.ok("点赞成功");
            } else {
                return ResponseEntity.badRequest().body("您已经点过赞了");
            }
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("点赞失败: " + e.getMessage());
        }
    }
    
    // 取消点赞帖子
    @DeleteMapping("/{forumId}/like")
    public ResponseEntity<?> unlikeForum(@Min(value = 1, message = "帖子ID必须大于0") @PathVariable int forumId, Authentication authentication) {
        try {
            // 检查用户是否已认证
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            boolean result = forumService.unlikeForum(forumId, user.id);
            if (result) {
                return ResponseEntity.ok("取消点赞成功");
            } else {
                return ResponseEntity.badRequest().body("您尚未点赞");
            }
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("取消点赞失败: " + e.getMessage());
        }
    }
    
    // 收藏帖子
    @PostMapping("/{forumId}/favorite")
    public ResponseEntity<?> favoriteForum(@Min(value = 1, message = "帖子ID必须大于0") @PathVariable int forumId, Authentication authentication) {
        try {
            // 检查用户是否已认证
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            boolean result = forumService.favoriteForum(forumId, user.id);
            if (result) {
                return ResponseEntity.ok("收藏成功");
            } else {
                return ResponseEntity.badRequest().body("您已经收藏过了");
            }
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("收藏失败: " + e.getMessage());
        }
    }
    
    // 取消收藏帖子
    @DeleteMapping("/{forumId}/favorite")
    public ResponseEntity<?> unfavoriteForum(@Min(value = 1, message = "帖子ID必须大于0") @PathVariable int forumId, Authentication authentication) {
        try {
            // 检查用户是否已认证
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            boolean result = forumService.unfavoriteForum(forumId, user.id);
            if (result) {
                return ResponseEntity.ok("取消收藏成功");
            } else {
                return ResponseEntity.badRequest().body("您尚未收藏");
            }
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("取消收藏失败: " + e.getMessage());
        }
    }
    
    // 添加评论
    @PostMapping("/{forumId}/comment")
    public ResponseEntity<?> addComment(@Min(value = 1, message = "帖子ID必须大于0") @PathVariable int forumId, 
                                       @RequestParam String content, 
                                       Authentication authentication) {
        try {
            // 检查用户是否已认证
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("评论内容不能为空");
            }
            
            int commentId = forumService.addComment(forumId, user.id, content);
            return ResponseEntity.ok("评论成功，评论ID: " + commentId);
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("评论失败: " + e.getMessage());
        }
    }
    
    // 管理员删除评论（软删除）
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<?> deleteComment(@Min(value = 1, message = "评论ID必须大于0") @PathVariable int commentId, Authentication authentication) {
        try {
            // 检查用户是否已认证
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null || user.type != 2) {
                return ResponseEntity.badRequest().body("权限不足");
            }
            
            boolean result = forumService.deleteComment(commentId);
            if (result) {
                return ResponseEntity.ok("删除评论成功");
            } else {
                return ResponseEntity.badRequest().body("评论不存在");
            }
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("删除评论失败: " + e.getMessage());
        }
    }
    
    // 获取用户所有点赞的帖子ID
    @GetMapping("/user/liked")
    public ResponseEntity<?> getUserLikedForums(Authentication authentication) {
        try {
            // 检查用户是否已认证
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            List<Integer> likedForums = forumService.getUserLikedForums(user.id);
            return ResponseEntity.ok(likedForums);
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("获取点赞帖子失败: " + e.getMessage());
        }
    }
    
    // 获取用户所有收藏的帖子ID
    @GetMapping("/user/favorited")
    public ResponseEntity<?> getUserFavoritedForums(Authentication authentication) {
        try {
            // 检查用户是否已认证
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            List<Integer> favoritedForums = forumService.getUserFavoritedForums(user.id);
            return ResponseEntity.ok(favoritedForums);
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("获取收藏帖子失败: " + e.getMessage());
        }
    }

    @PostMapping("{forum_id}/picture")
    public ResponseEntity<?> addPicture(@Min(value = 1, message = "帖子ID必须大于0") @PathVariable int forum_id, Authentication authentication, @Valid @RequestBody ForumPictureRequest forumPictureRequest){
        try{
            if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            Forum forum = forumService.getForumById(forum_id);
            if (forum == null || forum.getId() == null) {
                return ResponseEntity.status(404).body("帖子不存在");
            }
            String username = authentication.getName();
            if (!forum.username.equals(username) && !authentication.getAuthorities().contains("ADMIN")) {
                return ResponseEntity.status(403).body("权限不足（非帖子发表者）");
            }
            
            List<String> pictures = new ArrayList<>();
            pictures.add(forumPictureRequest.getPicture1());
            if (forumPictureRequest.getPicture2() != null) {
                pictures.add(forumPictureRequest.getPicture2());
            }
            if (forumPictureRequest.getPicture3() != null) {
                pictures.add(forumPictureRequest.getPicture3());
            }
            if (forumPictureRequest.getPicture4() != null) {
                pictures.add(forumPictureRequest.getPicture4());
            }
            if (forumPictureRequest.getPicture5() != null) {
                pictures.add(forumPictureRequest.getPicture5());
            }
            if (forumPictureRequest.getPicture6() != null) {
                pictures.add(forumPictureRequest.getPicture6());
            }
            if (forumPictureRequest.getPicture7() != null) {
                pictures.add(forumPictureRequest.getPicture7());
            }
            if (forumPictureRequest.getPicture8() != null) {
                pictures.add(forumPictureRequest.getPicture8());
            }
            if (forumPictureRequest.getPicture9() != null) {
                pictures.add(forumPictureRequest.getPicture9());
            }
            
            String picturePaths = forumService.savePicture(pictures, "./external/static/forum_pic/");
            if (picturePaths == null) {
                return ResponseEntity.badRequest().body("图片保存失败");
            }
            
            ForumRepository.addPictureToForum(forum_id, picturePaths);
            return ResponseEntity.ok("图片添加成功");
        } catch (SQLException e) {
            logger.error("添加图片失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("添加图片失败: " + e.getMessage());
        }
    }

    @DeleteMapping("{forum_id}/picture")
    public ResponseEntity<?> deletePicture(@Min(value = 1, message = "帖子ID必须大于0") @PathVariable int forum_id, Authentication authentication){
        try{
            if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(403).body("请先登录");
            }
            
            Forum forum = forumService.getForumById(forum_id);
            if (forum == null || forum.getId() == null) {
                return ResponseEntity.status(404).body("帖子不存在");
            }
            
            String username = authentication.getName();
            if (!forum.username.equals(username) && !authentication.getAuthorities().contains("ADMIN")) {
                return ResponseEntity.status(403).body("权限不足（非帖子发表者）");
            }
            
            if (!ForumRepository.removePictureFromForum(forum_id)){
                return ResponseEntity.status(404).body("帖子没有关联的图片");
            }
            return ResponseEntity.ok("图片删除成功");
        } catch (SQLException e) {
            logger.error("删除图片失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("删除图片失败: " + e.getMessage());
        }
    }

    @GetMapping("/{forum_id}/picture/")
    public ResponseEntity<?> getPictureCount(@Min(value = 1, message = "帖子ID必须大于0") @PathVariable int forum_id){
        try{
            List<String> picturePaths = ForumRepository.getPicturePathsFromForum(forum_id);
            if (picturePaths == null || picturePaths.isEmpty()) {
                return ResponseEntity.status(404).body("帖子没有关联的图片");
            }
            return ResponseEntity.ok(picturePaths.size());
        } catch (SQLException e) {
            logger.error("获取图片数量失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取图片数量失败: " + e.getMessage());
        }
    }

    @GetMapping("/{forum_id}/picture/{id}")
    public ResponseEntity<?> getPicture(@Min(value = 1, message = "帖子ID必须大于0") @PathVariable int forum_id, @Min(value = 1, message = "图片ID必须大于0") @Max(value = 9, message = "图片ID必须小于10") @PathVariable int id){
        try{
            List<String> picturePaths = ForumRepository.getPicturePathsFromForum(forum_id);
            if (picturePaths == null || picturePaths.isEmpty() || picturePaths.size() < id) {
                return ResponseEntity.status(404).body("图片不存在");
            }
            String picturePath = picturePaths.get(id - 1);
            if (picturePath == null || picturePath.isEmpty()) {
                return ResponseEntity.status(404).body("图片不存在");
            }
            Path imagePath = Paths.get("./external/static/forum_pic/").resolve(picturePath).normalize();
            Path allowedDir = Paths.get("./external/static/forum_pic/").toAbsolutePath().normalize();
            if (!imagePath.toAbsolutePath().normalize().startsWith(allowedDir)) {
                return ResponseEntity.status(403).body("非法文件访问尝试");
            }
            if (!Files.exists(imagePath)) {
                return ResponseEntity.status(404).body("图片文件不存在");
            }
            byte[] imageBytes = Files.readAllBytes(imagePath);
            MediaType mediaType = MediaType.IMAGE_PNG;
            if (picturePath.endsWith(".jpg") || picturePath.endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            }
            return ResponseEntity.ok().contentType(mediaType).body(imageBytes);
        } catch (SQLException | IOException e) {
            logger.error("获取图片失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("获取图片失败: " + e.getMessage());
        }
    }

    /*潜在的问题：
        搜索功能应该增加对标题的搜索，并考虑添加全文搜索功能以提高性能。
        创建带点位帖子功能需要添加事务管理来保证数据一致性，并改善错误处理机制。
        对于两个功能都需要加强输入验证和错误处理，提升系统的健壮性和安全性。
    */
    @GetMapping("/search")
    public ResponseEntity<?> searchForums(@RequestParam String keyword, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(403).body("请先登录");
        }
        try {
            List<Integer> result = ForumRepository.getForumsBySubstring(keyword);
            if (result == null || result.isEmpty()) {
                return ResponseEntity.ok("没有找到相关帖子。");
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("搜索帖子失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("搜索帖子失败: " + e.getMessage());
        }
    }

    @GetMapping("/create_with_point")
    public ResponseEntity<?> createForumWithPoint(@Valid @RequestBody ForumAndPointRequest forumAndPointRequest, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(403).body("请先登录");
        }
        int userid = userRepository.findByUsername(authentication.getName()).id;
        try {
            String dir = "";
            if (forumAndPointRequest.getPicture() != null && !forumAndPointRequest.getPicture().isEmpty()) {
                dir = forumService.savePicture(Collections.singletonList(forumAndPointRequest.getPicture()), "./external/static/description/");
                if (dir != null && dir.endsWith(",")) {
                    dir = dir.substring(0, dir.length() - 1);
                } else if (dir == null) {
                    logger.error("保存图片失败。但继续尝试保存帖子......");
                }
            }

            if (pointService.savePoint(userid, forumAndPointRequest.getX(), forumAndPointRequest.getY(), forumAndPointRequest.getLevel(), forumAndPointRequest.getType(), forumAndPointRequest.getDescription(), dir) == -1){
                logger.error("创建帖子失败");
                return ResponseEntity.badRequest().body("创建帖子失败");
            }
            forumService.createForum(userid, "标点帖", forumAndPointRequest.getDescription(), 2);
            return ResponseEntity.ok("创建点位帖子成功");
        } catch (Exception e) {
            logger.error("创建帖子且标点失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body("创建帖子且标点失败: " + e.getMessage());
        }
    }
}