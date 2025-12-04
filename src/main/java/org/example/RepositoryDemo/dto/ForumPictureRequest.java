package org.example.RepositoryDemo.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public class ForumPictureRequest {
    @Getter
    @Setter
    @NotNull(message = "图片1不能为空。至少得有一张照片")
    private String picture1;

    @Getter
    @Setter
    private String picture2;

    @Getter
    @Setter
    private String picture3;

    @Getter
    @Setter
    private String picture4;

    @Getter
    @Setter
    private String picture5;

    @Getter
    @Setter
    private String picture6;

    @Getter
    @Setter
    private String picture7;

    @Getter
    @Setter
    private String picture8;

    @Getter
    @Setter
    private String picture9;
}
