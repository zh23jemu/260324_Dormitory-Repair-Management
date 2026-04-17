package com.platform.warranty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.ResourceQueryDTO;
import com.platform.warranty.domain.entity.Resource;
import com.platform.warranty.service.IResourceService;
import com.platform.warranty.utils.AjaxResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2025-05-10
 */
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final IResourceService resourceService;

    @GetMapping("/list")
    public AjaxResult<Page<Resource>> list(ResourceQueryDTO resourceQueryDTO){
        Page<Resource> page = resourceService.page(new Page<>(resourceQueryDTO.getPageNum(), resourceQueryDTO.getPageSize()));
        this.resourceService.lambdaQuery()
                .like(StringUtils.isNotBlank(resourceQueryDTO.getKeyword()), Resource::getTitle, resourceQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(resourceQueryDTO.getKeyword()), Resource::getDescription, resourceQueryDTO.getKeyword())
                .page(page);
        return AjaxResult.success(page);
    }
    @DeleteMapping("/deleteById/{id}")
    public AjaxResult<Void> deleteById(@PathVariable Long id){
        boolean result = resourceService.removeById(id);
        return result ? AjaxResult.success() : AjaxResult.error();
    }
    @GetMapping("/getById/{id}")
    public AjaxResult<Resource> getById(@PathVariable Long id){
        Resource resource = resourceService.getById(id);
        return AjaxResult.success(resource);
    }
    @PostMapping("/save")
    public AjaxResult<Void> save(@RequestBody Resource resource){
        boolean result = resourceService.saveOrUpdate(resource);
        return result ? AjaxResult.success() : AjaxResult.error();
    }
}
