package org.example.RepositoryDemo.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.service.ForumService;
import org.example.RepositoryDemo.Repository.UserRepository;
import org.example.RepositoryDemo.dto.ForumRequest;
import org.example.RepositoryDemo.entity.Forum;
import org.example.RepositoryDemo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/forum")
@Validated
public class ForumController {

    private static final Logger logger = LogManager.getLogger(ForumController.class);
    
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
                return ResponseEntity.badRequest().body("用户未登录");
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

    // 获取所有论坛帖子（不需要管理员权限）
    @GetMapping("/all")
    public ResponseEntity<?> getAllForums(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户未登录");
            }

            List<Forum> forums = forumService.getAllForums();
            return ResponseEntity.ok(forums);
        } catch (SQLException e) {
            return ResponseEntity.badRequest().body("获取论坛帖子失败: " + e.getMessage());
        }
    }

    // 根据用户ID获取论坛帖子（不需要管理员权限）
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getForumsByUserId(@Min(value = 1, message = "用户ID必须大于0") @PathVariable int userId, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户未登录");
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
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户未登录");
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
}