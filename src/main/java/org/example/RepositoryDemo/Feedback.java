package org.example.RepositoryDemo;

import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Feedback {
    public Integer id;
    public Integer userId;          // 用户ID，可为空（未登录用户）
    public String username;         // 用户名，可为空
    public String content;          // 反馈内容
    public String type;             // 反馈类型：system-系统错误, user-用户反馈
    public String url;              // 出错页面URL
    public String userAgent;        // 用户浏览器信息
    public String stackTrace;       // 错误堆栈信息（仅系统错误）
    public Timestamp createTime;    // 创建时间
    public Boolean resolved;        // 是否已解决
    public String resolvedBy;       // 解决人
    public Timestamp resolvedTime;  // 解决时间
}