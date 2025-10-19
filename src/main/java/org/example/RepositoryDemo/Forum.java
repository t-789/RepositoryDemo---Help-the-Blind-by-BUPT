package org.example.RepositoryDemo;

import java.sql.Timestamp;

public class Forum {
    public Integer id;
    public Integer user_id;
    public String title;
    public String content;
    public Timestamp release_time;
    public String username;

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
    
    // Getters and setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public Integer getUserId() {
        return user_id;
    }
    
    public void setUserId(Integer user_id) {
        this.user_id = user_id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Timestamp getReleaseTime() {
        return release_time;
    }
    
    public void setReleaseTime(Timestamp release_time) {
        this.release_time = release_time;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
}