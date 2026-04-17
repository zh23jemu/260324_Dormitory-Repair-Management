package com.platform.warranty.mapper;

import com.platform.warranty.domain.entity.StatisticsData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StatisticsMapper {
    /**
     * 统计各维修类型的报修数量
     * @return 维修类型统计列表
     */
    List<StatisticsData> getRepairTypeStatistics();

    /**
     * 统计各地点的报修数量
     * @return 地点统计列表
     */
    List<StatisticsData> getRepairLocationStatistics();

    /**
     * 根据时间范围统计各维修类型的报修数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 维修类型统计列表
     */
    List<StatisticsData> getRepairTypeStatisticsByTime(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 根据时间范围统计各地点的报修数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 地点统计列表
     */
    List<StatisticsData> getRepairLocationStatisticsByTime(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 统计总体报修记录数量
     * @return 总数量
     */
    Long getTotalRepairCount();

    /**
     * 统计各状态的报修记录数量
     * @return 状态统计列表
     */
    List<StatisticsData> getRepairStatusStatistics();
}