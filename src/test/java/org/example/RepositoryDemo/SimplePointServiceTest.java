package org.example.RepositoryDemo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SimplePointServiceTest {

    @Test
    public void testPointCreation() {
        Point point = new Point();
        point.id = 1;
        point.userId = 1;
        point.x = 100.0;
        point.y = 200.0;
        
        assertEquals(1, point.getId());
        assertEquals(1, point.getUserId());
        assertEquals(100.0, point.getX());
        assertEquals(200.0, point.getY());
    }

    @Test
    public void testUserCreation() {
        User user = new User();
        user.id = 1;
        user.username = "testuser";
        user.type = 0;
        
        assertEquals(1, user.id);
        assertEquals("testuser", user.username);
        assertEquals(0, user.type);
    }
}