package org.example.RepositoryDemo.entity;

public class Statistic {
    public int point_count;
    public int user_count;
    public int post_count;

    public Statistic(int point_count, int user_count, int post_count) {
        this.point_count = point_count;
        this.user_count = user_count;
        this.post_count = post_count;
    }
    public Statistic() {
    }
    
    // getter and setter methods
    public int getPoint_count() {
        return point_count;
    }
    
    public void setPoint_count(int point_count) {
        this.point_count = point_count;
    }
    
    public int getUser_count() {
        return user_count;
    }
    
    public void setUser_count(int user_count) {
        this.user_count = user_count;
    }
    
    public int getPost_count() {
        return post_count;
    }
    
    public void setPost_count(int post_count) {
        this.post_count = post_count;
    }
}