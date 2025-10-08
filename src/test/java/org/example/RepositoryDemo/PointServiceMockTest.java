package org.example.RepositoryDemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PointServiceMockTest {

    @Autowired
    private PointService pointService;

    @MockBean
    private PointRepository pointRepository;

    @Test
    public void testSavePoint() {
        // 模拟pointRepository.savePoint方法的行为
        Point mockPoint = new Point();
        mockPoint.id = 1;
        mockPoint.userId = 1;
        mockPoint.x = 100.0;
        mockPoint.y = 200.0;
        mockPoint.markedTime = new Timestamp(System.currentTimeMillis());
        mockPoint.deleted = false;
        mockPoint.proposeDelete = 0;

        when(pointRepository.savePoint(any(Point.class))).thenReturn(1);

        int result = pointService.savePoint(1, 100.0, 200.0);
        assertEquals(1, result);

        // 验证pointRepository的方法被调用了一次
        verify(pointRepository, times(1)).savePoint(any(Point.class));
    }

    @Test
    public void testGetAllActivePoints() {
        // 模拟pointRepository.getAllActivePoints方法的行为
        List<Point> mockPoints = new ArrayList<>();
        Point point = new Point();
        point.id = 1;
        point.userId = 1;
        point.x = 100.0;
        point.y = 200.0;
        point.markedTime = new Timestamp(System.currentTimeMillis());
        point.deleted = false;
        point.proposeDelete = 0;
        mockPoints.add(point);

        when(pointRepository.getAllActivePoints()).thenReturn(mockPoints);

        List<Point> result = pointService.getAllActivePoints();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100.0, result.get(0).x);
        assertEquals(200.0, result.get(0).y);

        // 验证pointRepository的方法被调用了一次
        verify(pointRepository, times(1)).getAllActivePoints();
    }

    @Test
    public void testProposeDeletePoint() {
        // 模拟pointRepository.proposeDeletePoint方法的行为
        when(pointRepository.proposeDeletePoint(1, 1)).thenReturn(1);

        int result = pointService.proposeDeletePoint(1, 1);
        assertEquals(1, result);

        // 验证pointRepository的方法被调用了一次
        verify(pointRepository, times(1)).proposeDeletePoint(1, 1);
    }

    @Test
    public void testAdminDeletePoint() {
        // 模拟pointRepository.adminDeletePoint方法的行为
        when(pointRepository.adminDeletePoint(1)).thenReturn(true);

        boolean result = pointService.adminDeletePoint(1);
        assertTrue(result);

        // 验证pointRepository的方法被调用了一次
        verify(pointRepository, times(1)).adminDeletePoint(1);
    }

    @Test
    public void testAdminRestorePoint() {
        // 模拟pointRepository.adminRestorePoint方法的行为
        when(pointRepository.adminRestorePoint(1)).thenReturn(true);

        boolean result = pointService.adminRestorePoint(1);
        assertTrue(result);

        // 验证pointRepository的方法被调用了一次
        verify(pointRepository, times(1)).adminRestorePoint(1);
    }

    @Test
    public void testGetAndSetDeleteThreshold() {
        // 模拟pointRepository.getDeleteThreshold方法的行为
        when(pointRepository.getDeleteThreshold()).thenReturn(5);

        int threshold = pointService.getDeleteThreshold();
        assertEquals(5, threshold);

        // 验证pointRepository的方法被调用了一次
        verify(pointRepository, times(1)).getDeleteThreshold();

        // 模拟pointRepository.setDeleteThreshold方法的行为
        when(pointRepository.setDeleteThreshold(10)).thenReturn(true);

        boolean setResult = pointService.setDeleteThreshold(10);
        assertTrue(setResult);

        // 验证pointRepository的方法被调用了一次
        verify(pointRepository, times(1)).setDeleteThreshold(10);
    }
}