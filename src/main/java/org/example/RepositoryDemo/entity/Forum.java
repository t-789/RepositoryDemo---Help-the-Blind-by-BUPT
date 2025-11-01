package org.example.RepositoryDemo.entity;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

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