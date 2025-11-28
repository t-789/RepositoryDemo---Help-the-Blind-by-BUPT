package org.example.RepositoryDemo.controller;

import org.example.RepositoryDemo.service.ForumService;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.dto.ForumRequest;
import org.example.RepositoryDemo.entity.Comment;
import org.example.RepositoryDemo.entity.Forum;
import org.example.RepositoryDemo.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/forum")
@Validated
public class ForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserRepository userRepository;

    // 创建论坛帖子
    @PostMapping("/create")
    public ResponseEntity<?> createForum(@Valid @RequestBody ForumRequest forumRequest, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(403).body("请先登录");
            }

            int result = forumService.createForum(user.id, forumRequest.getTitle(), forumRequest.getContent());
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
    
    // 点赞帖子
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
}