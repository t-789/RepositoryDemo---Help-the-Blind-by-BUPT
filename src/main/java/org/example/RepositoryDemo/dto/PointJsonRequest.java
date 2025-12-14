package org.example.RepositoryDemo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class PointJsonRequest {

    // getters and setters
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

    @NotNull(message = "等级不能为空")
    @DecimalMin(value = "1", message = "等级不能小于1")
    @DecimalMax(value = "3", message = "等级不能大于3")
    private Integer level;

    private String description;

    private String image;

}