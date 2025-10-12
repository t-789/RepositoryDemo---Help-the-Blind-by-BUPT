package org.example.RepositoryDemo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointRequest {
    
    @NotNull(message = "X坐标不能为空")
    @DecimalMin(value = "-1000.0", message = "X坐标不能小于-1000.0")
    @DecimalMax(value = "1000.0", message = "X坐标不能大于1000.0")
    private Double x;
    
    @NotNull(message = "Y坐标不能为空")
    @DecimalMin(value = "-1000.0", message = "Y坐标不能小于-1000.0")
    @DecimalMax(value = "1000.0", message = "Y坐标不能大于1000.0")
    private Double y;

    @NotNull(message = "类型不能为空")
    @DecimalMin(value = "1", message = "类型不能小于1")
    private Integer type;

    private String description;
    
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}