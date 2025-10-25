package org.example.RepositoryDemo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackRequest {
    private String content;
    private String url;
    private String userAgent;
    private String stackTrace;
}