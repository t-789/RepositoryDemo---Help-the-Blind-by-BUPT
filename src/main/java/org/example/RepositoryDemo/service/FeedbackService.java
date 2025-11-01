package org.example.RepositoryDemo.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.FeedbackRepository;
import org.example.RepositoryDemo.entity.Feedback;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;

@Service
public class FeedbackService {
    private static final Logger logger = LogManager.getLogger(FeedbackService.class);
    
    private final FeedbackRepository feedbackRepository;
    
    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }
    
    // 保存用户反馈
    public boolean saveUserFeedback(Integer userId, String username, String content, String url, String userAgent) {
        logger.debug("开始保存用户反馈: userId={}, username={}", userId, username);
        // 检查内容是否为空
        if (content == null || content.trim().isEmpty()) {
            content = "用户未提供具体反馈内容";
        }
        
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setUsername(username);
        feedback.setContent(content);
        feedback.setType("user");
        feedback.setUrl(url);
        feedback.setUserAgent(userAgent);
        feedback.setCreateTime(new Timestamp(System.currentTimeMillis()));
        feedback.setResolved(false);
        
        boolean result = feedbackRepository.saveFeedback(feedback);
        logger.debug("用户反馈保存结果: userId={}, username={}, result={}", userId, username, result);
        return result;
    }
    
    // 保存系统错误反馈
    public boolean saveSystemFeedback(Integer userId, String username, String content, String url, 
                                    String userAgent, String stackTrace) {
        // 检查内容是否为空
        if (content == null || content.trim().isEmpty()) {
            content = "未提供错误详情";
        }
        
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setUsername(username);
        feedback.setContent(content);
        feedback.setType("system");
        feedback.setUrl(url);
        feedback.setUserAgent(userAgent);
        feedback.setStackTrace(stackTrace);
        feedback.setCreateTime(new Timestamp(System.currentTimeMillis()));
        feedback.setResolved(false);
        
        return feedbackRepository.saveFeedback(feedback);
    }
    
    // 获取所有反馈
    public List<Feedback> getAllFeedback() {
        return feedbackRepository.getAllFeedback();
    }
    
    // 根据类型获取反馈
    public List<Feedback> getFeedbackByType(String type) {
        return feedbackRepository.getFeedbackByType(type);
    }
    
    // 根据解决状态获取反馈
    public List<Feedback> getFeedbackByResolvedStatus(boolean resolved) {
        return feedbackRepository.getFeedbackByResolvedStatus(resolved);
    }
    
    // 更新反馈解决状态
    public boolean updateResolvedStatus(int feedbackId, boolean resolved, String resolvedBy) {
        return feedbackRepository.updateResolvedStatus(feedbackId, resolved, resolvedBy);
    }
    
    // 根据ID获取反馈
    public Feedback getFeedbackById(int id) {
        return feedbackRepository.getFeedbackById(id);
    }
}