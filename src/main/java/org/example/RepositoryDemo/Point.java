// Point.java
package org.example.RepositoryDemo;

import java.sql.Timestamp;

public class Point {
    public Integer id;
    public Integer userId;
    public Double x;
    public Double y;
    public Timestamp markedTime;
    public Boolean deleted;
    public Timestamp deletedTime;
    public Integer proposeDelete;
    
    public Point() {
        // 默认构造函数
    }
    
    public Point(Integer id, Integer userId, Double x, Double y, Timestamp markedTime, 
                 Boolean deleted, Timestamp deletedTime, Integer proposeDelete) {
        this.id = id;
        this.userId = userId;
        this.x = x;
        this.y = y;
        this.markedTime = markedTime;
        this.deleted = deleted;
        this.deletedTime = deletedTime;
        this.proposeDelete = proposeDelete;
    }
    
    // getter和setter方法
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }
    
    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }
    
    public Timestamp getMarkedTime() { return markedTime; }
    public void setMarkedTime(Timestamp markedTime) { this.markedTime = markedTime; }
    
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    
    public Timestamp getDeletedTime() { return deletedTime; }
    public void setDeletedTime(Timestamp deletedTime) { this.deletedTime = deletedTime; }
    
    public Integer getProposeDelete() { return proposeDelete; }
    public void setProposeDelete(Integer proposeDelete) { this.proposeDelete = proposeDelete; }
}
