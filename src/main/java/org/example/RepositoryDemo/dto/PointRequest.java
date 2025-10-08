package org.example.RepositoryDemo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class PointRequest {
    
    @NotNull(message = "X坐标不能为空")
    @DecimalMin(value = "-1000.0", message = "X坐标不能小于-1000.0")
    @DecimalMax(value = "1000.0", message = "X坐标不能大于1000.0")
    private Double x;
    
    @NotNull(message = "Y坐标不能为空")
    @DecimalMin(value = "-1000.0", message = "Y坐标不能小于-1000.0")
    @DecimalMax(value = "1000.0", message = "Y坐标不能大于1000.0")
    private Double y;
    
    // getters and setters
    public Double getX() {
        return x;
    }
    
    public void setX(Double x) {
        this.x = x;
    }
    
    public Double getY() {
        return y;
    }
    
    public void setY(Double y) {
        this.y = y;
    }
}