package com.platform.warranty.service.impl;

import com.platform.warranty.domain.entity.StatisticsData;
import com.platform.warranty.mapper.StatisticsMapper;
import com.platform.warranty.service.IStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements IStatisticsService {

    private final StatisticsMapper statisticsMapper;

    @Override
    public List<StatisticsData> getRepairTypeStatistics() {
        return statisticsMapper.getRepairTypeStatistics();
    }

    @Override
    public List<StatisticsData> getRepairLocationStatistics() {
        return statisticsMapper.getRepairLocationStatistics();
    }

    @Override
    public List<StatisticsData> getRepairTypeStatisticsByTime(String startTime, String endTime) {
        return statisticsMapper.getRepairTypeStatisticsByTime(startTime, endTime);
    }

    @Override
    public List<StatisticsData> getRepairLocationStatisticsByTime(String startTime, String endTime) {
        return statisticsMapper.getRepairLocationStatisticsByTime(startTime, endTime);
    }

    @Override
    public Long getTotalRepairCount() {
        return statisticsMapper.getTotalRepairCount();
    }

    @Override
    public List<StatisticsData> getRepairStatusStatistics() {
        return statisticsMapper.getRepairStatusStatistics();
    }
}