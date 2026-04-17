package com.platform.warranty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.RepairTypeQueryDTO;
import com.platform.warranty.domain.entity.RepairType;
import com.platform.warranty.service.IRepairTypeService;
import com.platform.warranty.utils.AjaxResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 维修类型表 前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2025-05-14
 */
@RestController
@RequestMapping("/repairType")
@RequiredArgsConstructor
public class RepairTypeController {

     private final IRepairTypeService repairTypeService;
    @GetMapping("/list")
    private AjaxResult<Page<RepairType>> list(RepairTypeQueryDTO repairTypeQueryDTO){
        Page<RepairType> page = new Page<>(repairTypeQueryDTO.getPageNum(), repairTypeQueryDTO.getPageSize());
        repairTypeService.lambdaQuery()
                .like(StringUtils.isNotBlank(repairTypeQueryDTO.getKeyword()), RepairType::getName, repairTypeQueryDTO.getKeyword())
                .page(page);
        return AjaxResult.success(page);

    }
    @GetMapping("/getById/{id}")
    private AjaxResult<RepairType> getById(@PathVariable Long id){
        return AjaxResult.success(repairTypeService.getById(id));
    }
    @GetMapping("/deleteById/{id}")
    private AjaxResult<Void> deleteById(@PathVariable Long id){
        repairTypeService.removeById(id);
        return AjaxResult.success();
    }
    @PostMapping("/save")
    private AjaxResult<Void> save(@RequestBody RepairType repairType){
        repairTypeService.saveOrUpdate(repairType);
        return AjaxResult.success();
    }
}
