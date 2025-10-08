package org.example.RepositoryDemo;

import java.sql.Timestamp;

public class User {
    public Integer id;
    public String username;
    public String password_hash;
    public Integer type;
    public Boolean isBanned;
    public Timestamp banEndTime;

    public User() {
        // 默认构造函数
    }

    public User(String username, String password_hash, Integer type) {
        this.username = username;
        this.password_hash = password_hash;
        this.type = type;
    }

    public User(Integer id, String username, String password_hash, Integer type, Boolean isBanned, Timestamp banEndTime) {
        this.id = id;
        this.username = username;
        this.password_hash = password_hash;
        this.type = type;
        this.isBanned = isBanned;
        this.banEndTime = banEndTime;
    }

    public User User(User user){
        return assign(user);
    }

    public User assign(User user) {
        if (user == null) {
            return this;
        }
        if (this == user){
            return this;
        }
        this.id = user.id;
        this.username = user.username;
        this.password_hash = user.password_hash;
        this.type = user.type;
        this.isBanned = user.isBanned;
        this.banEndTime = user.banEndTime;
        return this;
    }

    // 构造函数、getter和setter方法
}
