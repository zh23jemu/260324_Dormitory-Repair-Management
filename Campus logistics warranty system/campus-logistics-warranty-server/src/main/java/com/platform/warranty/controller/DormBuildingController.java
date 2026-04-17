package com.platform.warranty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.DormBuildingQueryDTO;
import com.platform.warranty.domain.entity.DormBuilding;
import com.platform.warranty.service.IDormBuildingService;
import com.platform.warranty.utils.AjaxResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 宿舍楼信息表 前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2026-01-05
 */
@RestController
@RequestMapping("/dormBuilding")
@RequiredArgsConstructor
public class DormBuildingController {

    private final IDormBuildingService dormBuildingService;
    
    @GetMapping("/list")
    public AjaxResult<Page<DormBuilding>> list(DormBuildingQueryDTO dormBuildingQueryDTO) {
        Page<DormBuilding> page = new Page<>(dormBuildingQueryDTO.getPageNum(), dormBuildingQueryDTO.getPageSize());
        dormBuildingService.lambdaQuery()
                .like(StringUtils.isNotBlank(dormBuildingQueryDTO.getKeyword()), DormBuilding::getBuildingId, dormBuildingQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(dormBuildingQueryDTO.getKeyword()), DormBuilding::getBuildingName, dormBuildingQueryDTO.getKeyword())
                .eq(StringUtils.isNotBlank(dormBuildingQueryDTO.getBuildingId()), DormBuilding::getBuildingId, dormBuildingQueryDTO.getBuildingId())
                .eq(StringUtils.isNotBlank(dormBuildingQueryDTO.getBuildingName()), DormBuilding::getBuildingName, dormBuildingQueryDTO.getBuildingName())
                .eq(dormBuildingQueryDTO.getBuildingType() != null, DormBuilding::getBuildingType, dormBuildingQueryDTO.getBuildingType())
                .eq(dormBuildingQueryDTO.getStatus() != null, DormBuilding::getStatus, dormBuildingQueryDTO.getStatus())
                .orderByDesc(DormBuilding::getBuildingId)
                .page(page);
        return AjaxResult.success(page);
    }
    
    @GetMapping("/all")
    public AjaxResult<java.util.List<DormBuilding>> getAll() {
        java.util.List<DormBuilding> list = dormBuildingService.list();
        return AjaxResult.success(list);
    }
    
    @GetMapping("/getById/{id}")
    public AjaxResult<DormBuilding> getById(@PathVariable String id) {
        return AjaxResult.success(dormBuildingService.getById(id));
    }
    
    @PostMapping("/save")
    public AjaxResult<Void> save(@RequestBody DormBuilding dormBuilding) {
        dormBuildingService.saveOrUpdate(dormBuilding);
        return AjaxResult.success();
    }
    
    @PutMapping("/update")
    public AjaxResult<Void> update(@RequestBody DormBuilding dormBuilding) {
        dormBuildingService.saveOrUpdate(dormBuilding);
        return AjaxResult.success();
    }
    
    @DeleteMapping("/delete/{id}")
    public AjaxResult<Void> delete(@PathVariable String id) {
        dormBuildingService.removeById(id);
        return AjaxResult.success();
    }
}
