// Point.java
package org.example.RepositoryDemo.entity;

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
    public Integer confirmCount;
    public Integer level; // 风险等级：高3中2低1

    /* *
     * 点位类型（待定）：
     * 1:
     * 2: ？
     * 3: ？
     */
    public Integer type;
    public String description;
    
    // 添加类型名称字段，用于存储映射后的类型名称
    public String typeName;

    // 添加图片描述字段，用于存储图片描述
    public String image_description;
    
    public Point() {
        // 默认构造函数
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Point point = (Point) obj;
        return id.equals(point.id);
    }

}