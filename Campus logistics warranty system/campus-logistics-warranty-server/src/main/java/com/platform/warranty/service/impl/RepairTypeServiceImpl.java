package com.platform.warranty.service.impl;

import com.platform.warranty.domain.entity.RepairType;
import com.platform.warranty.mapper.RepairTypeMapper;
import com.platform.warranty.service.IRepairTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 维修类型表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2025-05-14
 */
@Service
public class RepairTypeServiceImpl extends ServiceImpl<RepairTypeMapper, RepairType> implements IRepairTypeService {

}
