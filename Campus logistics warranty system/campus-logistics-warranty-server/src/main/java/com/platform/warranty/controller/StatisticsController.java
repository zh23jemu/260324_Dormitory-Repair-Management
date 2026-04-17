package com.platform.warranty.controller;

import com.platform.warranty.domain.entity.StatisticsData;
import com.platform.warranty.service.IStatisticsService;
import com.platform.warranty.utils.AjaxResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 统计数据控制器，用于提供保修业务的统计数据
 * </p>
 *
 * @author system
 * @since 2026-01-05
 */
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final IStatisticsService statisticsService;

    /**
     * 获取维修类型统计信息
     */
    @GetMapping("/repairTypeStatistics")
    public AjaxResult<List<StatisticsData>> getRepairTypeStatistics() {
        List<StatisticsData> data = statisticsService.getRepairTypeStatistics();
        return AjaxResult.success(data);
    }

    /**
     * 获取维修地点统计信息
     */
    @GetMapping("/repairLocationStatistics")
    public AjaxResult<List<StatisticsData>> getRepairLocationStatistics() {
        List<StatisticsData> data = statisticsService.getRepairLocationStatistics();
        return AjaxResult.success(data);
    }

    /**
     * 根据时间范围获取维修类型统计信息
     */
    @GetMapping("/repairTypeStatisticsByTime")
    public AjaxResult<List<StatisticsData>> getRepairTypeStatisticsByTime(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        List<StatisticsData> data = statisticsService.getRepairTypeStatisticsByTime(startTime, endTime);
        return AjaxResult.success(data);
    }

    /**
     * 根据时间范围获取维修地点统计信息
     */
    @GetMapping("/repairLocationStatisticsByTime")
    public AjaxResult<List<StatisticsData>> getRepairLocationStatisticsByTime(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        List<StatisticsData> data = statisticsService.getRepairLocationStatisticsByTime(startTime, endTime);
        return AjaxResult.success(data);
    }

    /**
     * 获取总报修记录数量
     */
    @GetMapping("/totalRepairCount")
    public AjaxResult<Long> getTotalRepairCount() {
        Long count = statisticsService.getTotalRepairCount();
        return AjaxResult.success(count);
    }

    /**
     * 获取报修状态统计信息
     */
    @GetMapping("/repairStatusStatistics")
    public AjaxResult<List<StatisticsData>> getRepairStatusStatistics() {
        List<StatisticsData> data = statisticsService.getRepairStatusStatistics();
        return AjaxResult.success(data);
    }
}