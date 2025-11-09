// PointService.java
package org.example.RepositoryDemo.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.Repository.PointRepository;
import org.example.RepositoryDemo.entity.Point;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.List;

@Service
public class PointService {
    private static final Logger logger = LogManager.getLogger(PointService.class);
    @Autowired
    private FeedbackService feedbackService;
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
        try {
            List<Point> points = pointRepository.getAllActivePoints();
            java.util.Map<Integer, String> typeMaps = getAllTypeMaps();
            
            // 为每个点位设置类型名称
            for (Point point : points) {
                point.setTypeName(typeMaps.getOrDefault(point.getType(), 
                    point.getType() == 0 ? "未知" : String.valueOf(point.getType())));
            }
            
            return points;
        } catch (Exception e) {
            logger.error("获取所有活跃点位失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "获取所有活跃点位失败: " + e.getMessage(),
                        "/api/points/list",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return null;
        }
    }
    
    // 提议删除点位
    public int proposeDeletePoint(int pointId, int userId) {
        try {
            return pointRepository.proposeDeletePoint(pointId, userId);
        } catch (Exception e) {
            logger.error("提议删除点位失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "提议删除点位失败: " + e.getMessage(),
                        "/api/points/proposal",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return -3;
        }
    }

    // 确认点位
    public int confirmPoint(int pointId, int userId) {
        try {
            return pointRepository.ConfirmPoint(pointId, userId);
        } catch (Exception e) {
            logger.error("确认点位失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "确认点位失败: " + e.getMessage(),
                        "/api/points/confirm",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return -3;
        }
    }
    
    // 管理员删除点位
    public boolean adminDeletePoint(int pointId) {
        try {
            return pointRepository.adminDeletePoint(pointId);
        } catch (Exception e) {
            logger.error("管理员删除点位失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "管理员删除点位失败: " + e.getMessage(),
                        "/api/points/admin-delete",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return false;
        }
    }
    
    // 管理员恢复点位
    public boolean adminRestorePoint(int pointId) {
        try {
            return pointRepository.adminRestorePoint(pointId);
        } catch (Exception e) {
            logger.error("管理员恢复点位失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "管理员恢复点位失败: " + e.getMessage(),
                        "/api/points/admin-restore",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return false;
        }
    }
    
    // 获取删除阈值
    public int getDeleteThreshold() {
        try {
            return pointRepository.getDeleteThreshold();
        } catch (Exception e) {
            logger.error("获取删除阈值失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "获取删除阈值失败: " + e.getMessage(),
                        "/api/points/threshold",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return 5; // 默认阈值
        }
    }
    
    // 设置删除阈值
    public boolean setDeleteThreshold(int threshold) {
        try {
            return pointRepository.setDeleteThreshold(threshold);
        } catch (Exception e) {
            logger.error("设置删除阈值失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "设置删除阈值失败: " + e.getMessage(),
                        "/api/points/set-threshold",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return false;
        }
    }

// 获取所有点位（包括已删除的）
    public List<Point> getAllPoints() {
        try {
            List<Point> points = pointRepository.getAllPoints();
            java.util.Map<Integer, String> typeMaps = getAllTypeMaps();
            
            // 为每个点位设置类型名称
            for (Point point : points) {
                point.setTypeName(typeMaps.getOrDefault(point.getType(), 
                    point.getType() == 0 ? "未知" : String.valueOf(point.getType())));
            }
            
            return points;
        } catch (Exception e) {
            logger.error("获取所有点位失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "获取所有点位失败: " + e.getMessage(),
                        "/api/points/all",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return null;
        }
    }
    
    // 根据ID获取点位
    public Point getPointById(int pointId) {
        try {
            return pointRepository.getPointById(pointId);
        } catch (Exception e) {
            logger.error("根据ID获取点位失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "根据ID获取点位失败: " + e.getMessage(),
                        "/api/points/id",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return null;
        }
    }
    
    // 获取所有类型映射
    public java.util.Map<Integer, String> getAllTypeMaps() {
        try {
            return pointRepository.getAllTypeMaps();
        } catch (Exception e) {
            logger.error("获取所有类型映射失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "获取所有类型映射失败: " + e.getMessage(),
                        "/api/points/type-map-all",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return null;
        }
    }
    
    // 保存类型映射
    public boolean saveTypeMap(int typeId, String typeName) {
        try {
            return pointRepository.saveTypeMap(typeId, typeName);
        } catch (Exception e) {
            logger.error("保存类型映射失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "保存类型映射失败: " + e.getMessage(),
                        "/api/points/type-map",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return false;
        }
    }

    // 根据距离获取点位
    public List<Point> getPointsByDistance(double x, double y, double distance) {
        try {
            return pointRepository.getPointsByDistance(x, y, distance);
        } catch (Exception e) {
            logger.error("根据距离获取点位失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "根据距离获取点位失败: " + e.getMessage(),
                        "/api/points/distance",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return null;
        }
    }

    // 根据用户ID获取点位
    public List<Point> getPointsByUserId(int userId) {
        try {
            return pointRepository.getPointsByUserId(userId);
        } catch (Exception e) {
            logger.error("根据用户ID获取点位失败: {}", e.getMessage());
            try {
                if (feedbackService != null) {
                    feedbackService.saveSystemFeedback(
                        null,
                        "system",
                        "根据用户ID获取点位失败: " + e.getMessage(),
                        "/api/points/user",
                        "Point Service",
                        "Exception: " + e.getClass().getName() + "\nMessage: " + e.getMessage()
                    );
                }
            } catch (Exception fe) {
                logger.error("记录系统错误反馈时发生错误: {}", fe.getMessage());
            }
            return null;
        }
    }
}