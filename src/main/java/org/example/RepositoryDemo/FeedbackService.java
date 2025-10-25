package org.example.RepositoryDemo;

import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;

@Service
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    
    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }
    
    // 保存用户反馈
    public boolean saveUserFeedback(Integer userId, String username, String content, String url, String userAgent) {
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setUsername(username);
        feedback.setContent(content);
        feedback.setType("user");
        feedback.setUrl(url);
        feedback.setUserAgent(userAgent);
        feedback.setCreateTime(new Timestamp(System.currentTimeMillis()));
        feedback.setResolved(false);
        
        return feedbackRepository.saveFeedback(feedback);
    }
    
    // 保存系统错误反馈
    public boolean saveSystemFeedback(Integer userId, String username, String content, String url, 
                                    String userAgent, String stackTrace) {
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