// PointService.java
package org.example.RepositoryDemo.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.PointRepository;
import org.example.RepositoryDemo.entity.Point;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class PointService {
    private static final Logger logger = LogManager.getLogger(PointService.class);
    private final PointRepository pointRepository;
    
    public PointService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }
    
    // 保存点位
    public int savePoint(int userId, double x, double y, int level, int type, String description, String image) {
        logger.debug("开始保存点位: userId={}, x={}, y={}, level={}, type={}, description={}", userId, x, y, level, type, description);
        Point point = new Point();
        point.userId = userId;
        point.x = x;
        point.y = y;
        point.markedTime = new Timestamp(System.currentTimeMillis());
        point.deleted = false;
        point.proposeDelete = 0;
        point.confirmCount = 0;
        point.level = level;
        point.type = type;
        point.description = description;
        point.image_description = image;
        
        int result = pointRepository.savePoint(point);
        logger.debug("点位保存结果: userId={}, result={}", userId, result);
        return result;
    }
    
    // 获取所有活跃点位
    public List<Point> getAllActivePoints() {
        List<Point> points = pointRepository.getAllActivePoints();
        java.util.Map<Integer, String> typeMaps = getAllTypeMaps();
        
        // 为每个点位设置类型名称
        for (Point point : points) {
            point.setTypeName(typeMaps.getOrDefault(point.getType(), 
                point.getType() == 0 ? "未知" : String.valueOf(point.getType())));
        }
        
        return points;
    }
    
    // 提议删除点位
    public int proposeDeletePoint(int pointId, int userId) {
        return pointRepository.proposeDeletePoint(pointId, userId);
    }

    // 确认点位
    public int confirmPoint(int pointId, int userId) {
        return pointRepository.ConfirmPoint(pointId, userId);
    }
    
    // 管理员删除点位
    public boolean adminDeletePoint(int pointId) {
        return pointRepository.adminDeletePoint(pointId);
    }
    
    // 管理员恢复点位
    public boolean adminRestorePoint(int pointId) {
        return pointRepository.adminRestorePoint(pointId);
    }
    
    // 获取删除阈值
    public int getDeleteThreshold() {
        return pointRepository.getDeleteThreshold();
    }
    
    // 设置删除阈值
    public boolean setDeleteThreshold(int threshold) {
        return pointRepository.setDeleteThreshold(threshold);
    }

// 获取所有点位（包括已删除的）
    public List<Point> getAllPoints() {
        List<Point> points = pointRepository.getAllPoints();
        java.util.Map<Integer, String> typeMaps = getAllTypeMaps();
        
        // 为每个点位设置类型名称
        for (Point point : points) {
            point.setTypeName(typeMaps.getOrDefault(point.getType(), 
                point.getType() == 0 ? "未知" : String.valueOf(point.getType())));
        }
        
        return points;
    }
    
    // 根据ID获取点位
    public Point getPointById(int pointId) {
        return pointRepository.getPointById(pointId);
    }
    
    // 获取所有类型映射
    public java.util.Map<Integer, String> getAllTypeMaps() {
        return pointRepository.getAllTypeMaps();
    }
    
    // 保存类型映射
    public boolean saveTypeMap(int typeId, String typeName) {
        return pointRepository.saveTypeMap(typeId, typeName);
    }

    // 根据距离获取点位
    public List<Point> getPointsByDistance(double x, double y, double distance) {
        return pointRepository.getPointsByDistance(x, y, distance);
    }

    // 根据用户ID获取点位
    public List<Point> getPointsByUserId(int userId) {
        return pointRepository.getPointsByUserId(userId);
    }
}
