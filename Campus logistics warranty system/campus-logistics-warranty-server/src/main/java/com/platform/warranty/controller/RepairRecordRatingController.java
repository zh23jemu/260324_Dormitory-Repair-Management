package com.platform.warranty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.RepairRecordRatingDTO;
import com.platform.warranty.domain.entity.RepairRecord;
import com.platform.warranty.domain.entity.RepairRecordRating;
import com.platform.warranty.domain.entity.User;
import com.platform.warranty.service.IRepairRecordRatingService;
import com.platform.warranty.service.IRepairRecordService;
import com.platform.warranty.service.IUserService;
import com.platform.warranty.utils.AjaxResult;
import com.platform.warranty.utils.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 维修单评分表 前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2025-05-14
 */
@RestController
@RequestMapping("/repairRecordRating")
@RequiredArgsConstructor
public class RepairRecordRatingController {

    private final IRepairRecordService repairRecordService;
    private final IRepairRecordRatingService repairRecordRatingService;
    private final IUserService userService;

    @PostMapping("/save")
    public AjaxResult<Void> save(@RequestBody RepairRecordRating repairRecordRating, HttpServletRequest request) {
        // 先查是否已经评分过
        Long userId = CommonUtils.getUserId(request);
        RepairRecordRating one = repairRecordRatingService.lambdaQuery()
                .eq(RepairRecordRating::getUserId, userId)
                .eq(RepairRecordRating::getRepairRecordId, repairRecordRating.getRepairRecordId()).one();
        if (one != null) {
            return AjaxResult.error("您已评价过");
        }
        if (repairRecordRating.getUserId() == null){
            repairRecordRating.setUserId(userId);
        }
        repairRecordRatingService.save(repairRecordRating);
        return AjaxResult.success();
    }
    @GetMapping("/list")
    public AjaxResult<Page<RepairRecordRating>> list(RepairRecordRatingDTO repairRecordRatingDTO, HttpServletRequest request) {
        Page<RepairRecordRating> page = new Page<>(repairRecordRatingDTO.getPageNum(), repairRecordRatingDTO.getPageSize());
        Long userId = CommonUtils.getUserId(request);
        User user = userService.getById(userId);
        if (user.getRole().equalsIgnoreCase("admin")) {
            repairRecordRatingService.lambdaQuery()
                    .like(StringUtils.isNotBlank(repairRecordRatingDTO.getKeyword()), RepairRecordRating::getComment, repairRecordRatingDTO.getKeyword())
                    .page(page);
            for (RepairRecordRating record : page.getRecords()) {
                Long recordUserId = record.getUserId();
                User byId = userService.getById(recordUserId);
                if (byId != null) {
                    record.setUserName(byId.getName());
                }
                Long repairRecordId = record.getRepairRecordId();
                RepairRecord repairRecord = repairRecordService.getById(repairRecordId);
                if (repairRecord != null) {
                    record.setRepairRecordName(repairRecord.getName());
                }
            }
            return AjaxResult.success(page);
        }
        if (user.getRole().equalsIgnoreCase("worker")) {
            List<RepairRecord> list = repairRecordService.lambdaQuery()
                    .eq(RepairRecord::getWorkerId, userId)
                    .list();
            Set<Long> collect = list.stream().map(RepairRecord::getId).collect(Collectors.toSet());
            repairRecordRatingService.lambdaQuery()
                    .in(RepairRecordRating::getRepairRecordId, collect)
                .like(StringUtils.isNotBlank(repairRecordRatingDTO.getKeyword()), RepairRecordRating::getComment, repairRecordRatingDTO.getKeyword())
                .page(page);
            for (RepairRecordRating record : page.getRecords()) {
            Long recordUserId = record.getUserId();
            User byId = userService.getById(recordUserId);
            if (byId != null) {
                record.setUserName(byId.getName());
            }
            Long repairRecordId = record.getRepairRecordId();
            RepairRecord repairRecord = repairRecordService.getById(repairRecordId);
            if (repairRecord != null) {
                record.setRepairRecordName(repairRecord.getName());
            }
        }
            return AjaxResult.success(page);
        }
        repairRecordRatingService.lambdaQuery()
//                .eq(RepairRecordRating::getUserId, userId)
                .eq(Objects.nonNull(repairRecordRatingDTO.getRepairRecordId()),
                        RepairRecordRating::getRepairRecordId, repairRecordRatingDTO.getRepairRecordId())
                .like(StringUtils.isNotBlank(repairRecordRatingDTO.getKeyword()), RepairRecordRating::getComment, repairRecordRatingDTO.getKeyword())
                .page(page);

        for (RepairRecordRating record : page.getRecords()) {
            Long recordUserId = record.getUserId();
            User byId = userService.getById(recordUserId);
            if (byId != null) {
                record.setUserName(byId.getName());
            }
            Long repairRecordId = record.getRepairRecordId();
            RepairRecord repairRecord = repairRecordService.getById(repairRecordId);
            if (repairRecord != null) {
                record.setRepairRecordName(repairRecord.getName());
            }
        }
        return AjaxResult.success(page);
    }

}
