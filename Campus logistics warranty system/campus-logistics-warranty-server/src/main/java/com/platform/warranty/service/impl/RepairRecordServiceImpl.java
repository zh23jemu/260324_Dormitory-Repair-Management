package com.platform.warranty.service.impl;

import com.platform.warranty.domain.entity.RepairRecord;
import com.platform.warranty.mapper.RepairRecordMapper;
import com.platform.warranty.service.IRepairRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 维修记录表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2025-05-14
 */
@Service
public class RepairRecordServiceImpl extends ServiceImpl<RepairRecordMapper, RepairRecord> implements IRepairRecordService {

}
