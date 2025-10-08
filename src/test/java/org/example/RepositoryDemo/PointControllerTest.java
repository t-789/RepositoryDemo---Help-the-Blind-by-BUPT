package org.example.RepositoryDemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.RepositoryDemo.dto.PointRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
public class PointControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSavePointWithValidData() throws Exception {
        PointRequest pointRequest = new PointRequest();
        pointRequest.setX(100.0);
        pointRequest.setY(200.0);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/points/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pointRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("点位保存成功"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSavePointWithInvalidData() throws Exception {
        PointRequest pointRequest = new PointRequest();
        pointRequest.setX(2000.0); // 超出范围
        pointRequest.setY(200.0);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/points/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pointRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void testGetActivePoints() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/points/active")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testProposeDeletePoint() throws Exception {
        // 先创建一个点位
        PointRequest pointRequest = new PointRequest();
        pointRequest.setX(150.0);
        pointRequest.setY(250.0);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/points/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pointRequest)))
                .andExpect(status().isOk());

        // 然后提议删除这个点位
        mockMvc.perform(MockMvcRequestBuilders.post("/api/points/1/propose-delete")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("提议删除成功"));
    }

    @Test
    @WithMockUser
    public void testAccessDeniedForAdminEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/points/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}