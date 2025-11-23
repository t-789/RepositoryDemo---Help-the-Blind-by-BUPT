package org.example.RepositoryDemo.entity;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

public class Comment {
    @Setter
    @Getter
    private Integer id;
    
    @Setter
    @Getter
    private Integer forumId;
    
    @Setter
    @Getter
    private Integer userId;
    
    @Setter
    @Getter
    private String content;
    
    @Setter
    @Getter
    private Timestamp commentTime;
    
    @Setter
    @Getter
    private Boolean deleted;
    
    @Setter
    @Getter
    private String username;
    
    public Comment() {
    }
    
    public Comment(Integer id, Integer forumId, Integer userId, String content, Timestamp commentTime, Boolean deleted, String username) {
        this.id = id;
        this.forumId = forumId;
        this.userId = userId;
        this.content = content;
        this.commentTime = commentTime;
        this.deleted = deleted;
        this.username = username;
    }
}