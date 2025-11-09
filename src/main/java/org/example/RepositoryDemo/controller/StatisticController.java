package org.example.RepositoryDemo.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.RepositoryDemo.entity.Statistic;
import org.example.RepositoryDemo.service.StatisticService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/statistic")
@Validated
public class StatisticController {
    private static final Logger logger = LogManager.getLogger(StatisticController.class);

    @Autowired
    private StatisticService statisticService;

    @RequestMapping("/get")
    public ResponseEntity<?> getStatistic() {
        try {
            Statistic statistic = statisticService.getStatistic();
            if (statistic != null){
                logger.info("获取统计信息成功！");
                return ResponseEntity.ok(statistic);
            } else {
                logger.error("获取统计信息失败！");
                return ResponseEntity.status(500).body("获取统计信息失败");
            }
        } catch (Exception e) {
            logger.error("获取统计信息时发生异常: {}", e.getMessage());
            return ResponseEntity.status(500).body("获取统计信息时发生异常: " + e.getMessage());
        }
    }
}