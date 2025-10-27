package org.example.RepositoryDemo;

import java.sql.Timestamp;

public class User {
    public Integer id;
    public String username;
    public String password_hash;
    public Integer type;
    public int credit;
    public Boolean isBanned;
    public Timestamp banEndTime;
    public String avatar; // 添加头像字段

    public User() {
        // 默认构造函数
    }

    public User(String username, String password_hash, Integer type) {
        this.username = username;
        this.password_hash = password_hash;
        this.type = type;
    }

    public User(Integer id, String username, String password_hash, Integer type, Integer credit, Boolean isBanned, Timestamp banEndTime) {
        this.id = id;
        this.username = username;
        this.password_hash = password_hash;
        this.type = type;
        this.credit = credit;
        this.isBanned = isBanned;
        this.banEndTime = banEndTime;
    }

    public User(Integer id, String username, String password_hash, Integer type, Integer credit, Boolean isBanned, Timestamp banEndTime, String avatar) {
        this.id = id;
        this.username = username;
        this.password_hash = password_hash;
        this.type = type;
        this.credit = credit;
        this.isBanned = isBanned;
        this.banEndTime = banEndTime;
        this.avatar = avatar;
    }

    public User(User user){
        this.id = user.id;
        this.username = user.username;
        this.password_hash = user.password_hash;
        this.type = user.type;
        this.credit = user.credit;
        this.isBanned = user.isBanned;
        this.banEndTime = user.banEndTime;
        this.avatar = user.avatar;
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
        this.credit = user.credit;
        this.isBanned = user.isBanned;
        this.banEndTime = user.banEndTime;
        this.avatar = user.avatar;
        return this;
    }

    // 构造函数、getter和setter方法
}