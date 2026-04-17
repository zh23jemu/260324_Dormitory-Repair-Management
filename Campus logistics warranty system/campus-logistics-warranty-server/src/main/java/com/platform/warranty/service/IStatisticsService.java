package com.platform.warranty.service;

import com.platform.warranty.domain.entity.StatisticsData;

import java.util.List;

public interface IStatisticsService {
    /**
     * 获取维修类型统计信息
     * @return 维修类型统计列表
     */
    List<StatisticsData> getRepairTypeStatistics();

    /**
     * 获取维修地点统计信息
     * @return 维修地点统计列表
     */
    List<StatisticsData> getRepairLocationStatistics();

    /**
     * 根据时间范围获取维修类型统计信息
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 维修类型统计列表
     */
    List<StatisticsData> getRepairTypeStatisticsByTime(String startTime, String endTime);

    /**
     * 根据时间范围获取维修地点统计信息
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 维修地点统计列表
     */
    List<StatisticsData> getRepairLocationStatisticsByTime(String startTime, String endTime);

    /**
     * 获取总报修记录数量
     * @return 总数量
     */
    Long getTotalRepairCount();

    /**
     * 获取报修状态统计信息
     * @return 状态统计列表
     */
    List<StatisticsData> getRepairStatusStatistics();
}