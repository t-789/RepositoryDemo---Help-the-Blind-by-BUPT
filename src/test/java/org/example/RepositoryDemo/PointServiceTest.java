package org.example.RepositoryDemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PointServiceTest {

    @Autowired
    private PointService pointService;

    @Test
    public void testSavePoint() {
        int pointId = pointService.savePoint(1, 100.0, 200.0);
        assertTrue(pointId > 0);
    }

    @Test
    public void testGetAllActivePoints() {
        // 保存一个点位
        pointService.savePoint(1, 100.0, 200.0);

        // 获取所有活跃点位
        List<Point> points = pointService.getAllActivePoints();
        assertNotNull(points);
        assertFalse(points.isEmpty());
        assertEquals(100.0, points.get(0).x);
        assertEquals(200.0, points.get(0).y);
    }

    @Test
    public void testProposeDeletePoint() {
        // 保存一个点位
        int pointId = pointService.savePoint(1, 100.0, 200.0);

        // 提议删除
        int result = pointService.proposeDeletePoint(pointId, 1);
        assertEquals(1, result); // 成功提议删除
    }

    @Test
    public void testAdminDeletePoint() {
        // 保存一个点位
        int pointId = pointService.savePoint(1, 100.0, 200.0);

        // 管理员删除点位
        boolean result = pointService.adminDeletePoint(pointId);
        assertTrue(result);
    }

    @Test
    public void testAdminRestorePoint() {
        // 保存一个点位
        int pointId = pointService.savePoint(1, 100.0, 200.0);

        // 管理员删除点位
        pointService.adminDeletePoint(pointId);

        // 管理员恢复点位
        boolean result = pointService.adminRestorePoint(pointId);
        assertTrue(result);
    }

    @Test
    public void testGetAndSetDeleteThreshold() {
        // 获取当前阈值
        int currentThreshold = pointService.getDeleteThreshold();
        assertTrue(currentThreshold > 0);

        // 设置新阈值
        boolean setResult = pointService.setDeleteThreshold(10);
        assertTrue(setResult);

        // 验证新阈值
        int newThreshold = pointService.getDeleteThreshold();
        assertEquals(10, newThreshold);
    }
}