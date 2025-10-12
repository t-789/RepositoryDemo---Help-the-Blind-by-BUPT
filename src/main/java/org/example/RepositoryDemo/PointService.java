// PointService.java
package org.example.RepositoryDemo;

import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class PointService {
    private final PointRepository pointRepository;
    
    public PointService(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }
    
    // 保存点位
    public int savePoint(int userId, double x, double y, int type, String description) {
        Point point = new Point();
        point.userId = userId;
        point.x = x;
        point.y = y;
        point.markedTime = new Timestamp(System.currentTimeMillis());
        point.deleted = false;
        point.proposeDelete = 0;
        point.type = type;
        point.description = description;
        
        return pointRepository.savePoint(point);
    }
    
    // 获取所有活跃点位
    public List<Point> getAllActivePoints() {
        return pointRepository.getAllActivePoints();
    }
    
    // 提议删除点位
    public int proposeDeletePoint(int pointId, int userId) {
        return pointRepository.proposeDeletePoint(pointId, userId);
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
        return pointRepository.getAllPoints();
    }

}
