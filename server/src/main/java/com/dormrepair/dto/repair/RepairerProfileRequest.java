package com.dormrepair.dto.repair;

import java.util.List;

/**
 * 维修员个人信息保存请求。
 * 支持头像、手机号和多维修工种；工种编码来自 sys_dict 中 repair_work_type。
 */
public record RepairerProfileRequest(
        String phone,
        String avatar,
        List<String> workTypeCodes
) {
}
