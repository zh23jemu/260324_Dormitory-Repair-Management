package com.platform.warranty.domain.entity;

import lombok.Data;

@Data
public class StatisticsData {
    private String name; // 名称，如维修类型名称或地点
    private Long count;  // 统计数量
}