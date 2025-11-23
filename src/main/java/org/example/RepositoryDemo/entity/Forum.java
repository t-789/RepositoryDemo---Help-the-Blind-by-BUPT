package org.example.RepositoryDemo.entity;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

public class Forum {
    // Getters and setters
    @Setter
    @Getter
    public Integer id;
    @Setter
    @Getter
    public Integer user_id;
    @Setter
    @Getter
    public String title;
    @Setter
    @Getter
    public String content;
    @Setter
    @Getter
    public Timestamp release_time;
    @Setter
    @Getter
    public String username;
    
    @Setter
    @Getter
    public Integer like;
    
    @Setter
    @Getter
    public Integer favorite;
    
    @Setter
    @Getter
    public Integer comment;
    
    @Setter
    @Getter
    public Integer userLiked;
    
    @Setter
    @Getter
    public Integer userFavorited;
    
    @Setter
    @Getter
    public List<Comment> comments;

    public Forum() {
    }
    
    public Forum(Integer id, Integer user_id, String title, String content, Timestamp release_time, String username) {
        this.id = id;
        this.user_id = user_id;
        this.title = title;
        this.content = content;
        this.release_time = release_time;
        this.username = username;
    }
}