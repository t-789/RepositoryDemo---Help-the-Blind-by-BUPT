package org.example.RepositoryDemo.service;

import org.example.RepositoryDemo.Repository.StatisticRepository;
import org.example.RepositoryDemo.entity.Statistic;
import org.springframework.stereotype.Service;

@Service
public class StatisticService {
    public static boolean updateUserCount(int change) {
        return StatisticRepository.updateUserCount(change);
    }
    public static boolean updatePointCount(int change) {
        return StatisticRepository.updatePointCount(change);
    }
    public static boolean updatePostCount(int change) {
        return StatisticRepository.updatePostCount(change);
    }

    public Statistic getStatistic() {
        return StatisticRepository.getStatistic();
    }
}
