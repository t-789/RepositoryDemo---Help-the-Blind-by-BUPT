// Point.java
package org.example.RepositoryDemo;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class Point {
    public Integer id;
    public Integer userId;
    public Double x;
    public Double y;
    public Timestamp markedTime;
    public Boolean deleted;
    public Timestamp deletedTime;
    public Integer proposeDelete;

    /* *
     * 点位类型（待定）：
     * 1: 普通点位
     * 2: ？
     * 3: ？
     */
    public Integer type;
    public String description;
    
    public Point() {
        // 默认构造函数
    }
    
    public Point(Integer id, Integer userId, Double x, Double y, Timestamp markedTime, 
                 Boolean deleted, Timestamp deletedTime, Integer proposeDelete, Integer type, String description) {
        this.id = id;
        this.userId = userId;
        this.x = x;
        this.y = y;
        this.markedTime = markedTime;
        this.deleted = deleted;
        this.deletedTime = deletedTime;
        this.proposeDelete = proposeDelete;
        this.type = type;
        this.description = description;
    }

}
