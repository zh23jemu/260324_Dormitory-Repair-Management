package com.platform.warranty.mapper;

import com.platform.warranty.domain.entity.RepairRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.platform.warranty.domain.vo.RepairRecordCountOfType;

import java.util.List;


/**
 * <p>
 * 维修记录表 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-05-14
 */
public interface RepairRecordMapper extends BaseMapper<RepairRecord> {

    List<RepairRecordCountOfType> selectRepairRecordCountOfType();
}

