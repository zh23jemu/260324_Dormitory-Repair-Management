package com.platform.warranty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.RepairRecordQueryDTO;
import com.platform.warranty.domain.entity.RepairRecord;
import com.platform.warranty.domain.entity.User;
import com.platform.warranty.service.IRepairRecordService;
import com.platform.warranty.service.IUserService;
import com.platform.warranty.utils.AjaxResult;
import com.platform.warranty.utils.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 维修记录表 前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2025-05-14
 */
@RestController
@RequestMapping("/repairRecord")
@RequiredArgsConstructor
public class RepairRecordController {

    private final IRepairRecordService repairRecordService;
    private final IUserService  userService;
    @GetMapping("/list")
    private AjaxResult<Page<RepairRecord>> list(RepairRecordQueryDTO repairRecordQueryDTO, HttpServletRequest request){
        Page<RepairRecord> page = new Page<>(repairRecordQueryDTO.getPageNum(), repairRecordQueryDTO.getPageSize());
        Long userId = CommonUtils.getUserId(request);
        if (userService.getById(userId).getRole().equalsIgnoreCase("admin")){
            repairRecordService.lambdaQuery()
                .like(StringUtils.isNotBlank(repairRecordQueryDTO.getKeyword()), RepairRecord::getDescription, repairRecordQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(repairRecordQueryDTO.getKeyword()), RepairRecord::getLocation, repairRecordQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(repairRecordQueryDTO.getKeyword()), RepairRecord::getRemark, repairRecordQueryDTO.getKeyword())
                    .eq(repairRecordQueryDTO.getWorkerId()!=null, RepairRecord::getWorkerId, repairRecordQueryDTO.getWorkerId())
                    .eq(repairRecordQueryDTO.getTypeId()!=null, RepairRecord::getTypeId, repairRecordQueryDTO.getTypeId())
                    .orderByAsc(RepairRecord::getStatus)
                .page(page);
            page.getRecords().forEach(item -> {
                Long userId1 = item.getUserId();
                User byId = userService.getById(userId1);
                if (byId != null){
                    item.setUserName(byId.getName());
                }
                Long workerId = item.getWorkerId();
                User worker = userService.getById(workerId);
                if (worker != null){
                    item.setWorkerName(worker.getName());
                }
            });
            return AjaxResult.success(page);
        } else if (userService.getById(userId).getRole().equalsIgnoreCase("worker")) {
            repairRecordService.lambdaQuery()
                .like(StringUtils.isNotBlank(repairRecordQueryDTO.getKeyword()), RepairRecord::getDescription, repairRecordQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(repairRecordQueryDTO.getKeyword()), RepairRecord::getLocation, repairRecordQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(repairRecordQueryDTO.getKeyword()), RepairRecord::getRemark, repairRecordQueryDTO.getKeyword())
                .eq(RepairRecord::getWorkerId, userId)
                .eq(repairRecordQueryDTO.getWorkerId()!=null, RepairRecord::getWorkerId, repairRecordQueryDTO.getWorkerId())
                .eq(repairRecordQueryDTO.getTypeId()!=null, RepairRecord::getTypeId, repairRecordQueryDTO.getTypeId())
                .orderByAsc(RepairRecord::getStatus)
                .page(page);
        page.getRecords().forEach(item -> {
                Long userId1 = item.getUserId();
                User byId = userService.getById(userId1);
                if (byId != null){
                    item.setUserName(byId.getName());
                }
                Long workerId = item.getWorkerId();
                User worker = userService.getById(workerId);
                if (worker != null){
                    item.setWorkerName(worker.getName());
                }
            });
        return AjaxResult.success(page);
        }
        repairRecordService.lambdaQuery()
                .like(StringUtils.isNotBlank(repairRecordQueryDTO.getKeyword()), RepairRecord::getDescription, repairRecordQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(repairRecordQueryDTO.getKeyword()), RepairRecord::getLocation, repairRecordQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(repairRecordQueryDTO.getKeyword()), RepairRecord::getRemark, repairRecordQueryDTO.getKeyword())
                .eq(RepairRecord::getUserId, userId)
                .eq(repairRecordQueryDTO.getWorkerId()!=null, RepairRecord::getWorkerId, repairRecordQueryDTO.getWorkerId())
                .eq(repairRecordQueryDTO.getTypeId()!=null, RepairRecord::getTypeId, repairRecordQueryDTO.getTypeId())
                .orderByAsc(RepairRecord::getStatus)
                .page(page);
        page.getRecords().forEach(item -> {
                Long userId1 = item.getUserId();
                User byId = userService.getById(userId1);
                if (byId != null){
                    item.setUserName(byId.getName());
                }
                Long workerId = item.getWorkerId();
                User worker = userService.getById(workerId);
                if (worker != null){
                    item.setWorkerName(worker.getName());
                }
            });
        return AjaxResult.success(page);
    }
    @GetMapping("/getById/{id}")
    private AjaxResult<RepairRecord> getById(@PathVariable Long id){
        return AjaxResult.success(repairRecordService.getById(id));
    }
    @GetMapping("/deleteById/{id}")
    private AjaxResult<Void> deleteById(@PathVariable Long id){
        repairRecordService.removeById(id);
        return AjaxResult.success();
    }
    @PostMapping("/save")
    private AjaxResult<Void> save(@RequestBody RepairRecord repairRecord){
        repairRecordService.saveOrUpdate(repairRecord);
        return AjaxResult.success();
    }
    @GetMapping("/getByUserId/{workerId}")
    private AjaxResult<List<RepairRecord>> getByUserId(@PathVariable Long workerId){
        List<RepairRecord> list = repairRecordService.lambdaQuery()
                .eq(RepairRecord::getWorkerId, workerId)
                .list();
        return AjaxResult.success(list);
    }
}
