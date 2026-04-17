package com.platform.warranty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.DormRoomQueryDTO;
import com.platform.warranty.domain.entity.DormRoom;
import com.platform.warranty.service.IDormRoomService;
import com.platform.warranty.utils.AjaxResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 宿舍房间表 前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2026-01-05
 */
@RestController
@RequestMapping("/dormRoom")
@RequiredArgsConstructor
public class DormRoomController {

    private final IDormRoomService dormRoomService;
    
    @GetMapping("/list")
    public AjaxResult<Page<DormRoom>> list(DormRoomQueryDTO dormRoomQueryDTO) {
        Page<DormRoom> page = new Page<>(dormRoomQueryDTO.getPageNum(), dormRoomQueryDTO.getPageSize());
        dormRoomService.lambdaQuery()
                .like(StringUtils.isNotBlank(dormRoomQueryDTO.getKeyword()), DormRoom::getRoomId, dormRoomQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(dormRoomQueryDTO.getKeyword()), DormRoom::getRoomNumber, dormRoomQueryDTO.getKeyword())
                .eq(StringUtils.isNotBlank(dormRoomQueryDTO.getRoomId()), DormRoom::getRoomId, dormRoomQueryDTO.getRoomId())
                .eq(StringUtils.isNotBlank(dormRoomQueryDTO.getBuildingId()), DormRoom::getBuildingId, dormRoomQueryDTO.getBuildingId())
                .eq(dormRoomQueryDTO.getFloor() != null, DormRoom::getFloor, dormRoomQueryDTO.getFloor())
                .eq(StringUtils.isNotBlank(dormRoomQueryDTO.getRoomNumber()), DormRoom::getRoomNumber, dormRoomQueryDTO.getRoomNumber())
                .eq(dormRoomQueryDTO.getRoomType() != null, DormRoom::getRoomType, dormRoomQueryDTO.getRoomType())
                .eq(dormRoomQueryDTO.getStatus() != null, DormRoom::getStatus, dormRoomQueryDTO.getStatus())
                .orderByDesc(DormRoom::getRoomId)
                .page(page);
        return AjaxResult.success(page);
    }
    
    @GetMapping("/all")
    public AjaxResult<java.util.List<DormRoom>> getAll() {
        java.util.List<DormRoom> list = dormRoomService.list();
        return AjaxResult.success(list);
    }
    
    @GetMapping("/getById/{id}")
    public AjaxResult<DormRoom> getById(@PathVariable String id) {
        return AjaxResult.success(dormRoomService.getById(id));
    }
    
    @PostMapping("/save")
    public AjaxResult<Void> save(@RequestBody DormRoom dormRoom) {
        // 是否存在
        DormRoom one = dormRoomService.lambdaQuery().eq(DormRoom::getRoomId, dormRoom.getRoomId())
                .eq(DormRoom::getBuildingId, dormRoom.getBuildingId())
                .eq(DormRoom::getFloor, dormRoom.getFloor()).one();
        if (one != null) {
            return AjaxResult.error("已存在");
        }
        dormRoomService.saveOrUpdate(dormRoom);
        return AjaxResult.success();
    }
    
    @PutMapping("/update")
    public AjaxResult<Void> update(@RequestBody DormRoom dormRoom) {
        try {
            dormRoomService.saveOrUpdate(dormRoom);
        }catch (Exception e){
            return AjaxResult.error("已存在");
        }
        return AjaxResult.success();
    }
    
    @DeleteMapping("/delete/{id}")
    public AjaxResult<Void> delete(@PathVariable String id) {
        dormRoomService.removeById(id);
        return AjaxResult.success();
    }
}
