package org.example.RepositoryDemo.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForumAndPointRequest {
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

    // title is auto generated, so no need.
//    @Size(max = 100, message = "标题长度不能超过100个字符")
//    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "内容长度不能超过1000个字符")
    private String description; // same as forum content.

    private String picture; // same as point image_description.
}
