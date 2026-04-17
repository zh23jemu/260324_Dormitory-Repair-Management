package com.platform.warranty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.DormFacilityQueryDTO;
import com.platform.warranty.domain.entity.DormFacility;
import com.platform.warranty.service.IDormFacilityService;
import com.platform.warranty.utils.AjaxResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 宿舍物品/设施表 前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2026-01-05
 */
@RestController
@RequestMapping("/dormFacility")
@RequiredArgsConstructor
public class DormFacilityController {

    private final IDormFacilityService dormFacilityService;
    
    @GetMapping("/list")
    public AjaxResult<Page<DormFacility>> list(DormFacilityQueryDTO dormFacilityQueryDTO) {
        Page<DormFacility> page = new Page<>(dormFacilityQueryDTO.getPageNum(), dormFacilityQueryDTO.getPageSize());
        dormFacilityService.lambdaQuery()
                .like(StringUtils.isNotBlank(dormFacilityQueryDTO.getKeyword()), DormFacility::getFacilityName, dormFacilityQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(dormFacilityQueryDTO.getKeyword()), DormFacility::getBrand, dormFacilityQueryDTO.getKeyword())
                .eq(StringUtils.isNotBlank(dormFacilityQueryDTO.getRoomId()), DormFacility::getRoomId, dormFacilityQueryDTO.getRoomId())
                .eq(StringUtils.isNotBlank(dormFacilityQueryDTO.getFacilityName()), DormFacility::getFacilityName, dormFacilityQueryDTO.getFacilityName())
                .eq(dormFacilityQueryDTO.getFacilityType() != null, DormFacility::getFacilityType, dormFacilityQueryDTO.getFacilityType())
                .eq(StringUtils.isNotBlank(dormFacilityQueryDTO.getBrand()), DormFacility::getBrand, dormFacilityQueryDTO.getBrand())
                .eq(dormFacilityQueryDTO.getStatus() != null, DormFacility::getStatus, dormFacilityQueryDTO.getStatus())
                .orderByDesc(DormFacility::getId)
                .page(page);
        return AjaxResult.success(page);
    }
    
    @GetMapping("/all")
    public AjaxResult<java.util.List<DormFacility>> getAll() {
        java.util.List<DormFacility> list = dormFacilityService.list();
        return AjaxResult.success(list);
    }
    
    @GetMapping("/getById/{id}")
    public AjaxResult<DormFacility> getById(@PathVariable Long id) {
        return AjaxResult.success(dormFacilityService.getById(id));
    }
    
    @PostMapping("/save")
    public AjaxResult<Void> save(@RequestBody DormFacility dormFacility) {
        dormFacilityService.saveOrUpdate(dormFacility);
        return AjaxResult.success();
    }
    
    @PutMapping("/update")
    public AjaxResult<Void> update(@RequestBody DormFacility dormFacility) {
        dormFacilityService.saveOrUpdate(dormFacility);
        return AjaxResult.success();
    }
    
    @DeleteMapping("/delete/{id}")
    public AjaxResult<Void> delete(@PathVariable Long id) {
        dormFacilityService.removeById(id);
        return AjaxResult.success();
    }
}
