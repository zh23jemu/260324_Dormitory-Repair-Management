package com.platform.warranty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.NoticeQueryDTO;
import com.platform.warranty.domain.entity.Notice;
import com.platform.warranty.service.INoticeService;
import com.platform.warranty.utils.AjaxResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 公告表 前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2025-05-13
 */
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {
 private final INoticeService noticeService;
    @GetMapping("/list")
    private AjaxResult<Page<Notice>> list(NoticeQueryDTO noticeQueryDTO){
        Page<Notice> page = new Page<>(noticeQueryDTO.getPageNum(), noticeQueryDTO.getPageSize());
        noticeService.lambdaQuery()
                .like(StringUtils.isNotBlank(noticeQueryDTO.getKeyword()), Notice::getTitle, noticeQueryDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(noticeQueryDTO.getKeyword()), Notice::getContent, noticeQueryDTO.getKeyword())
                .orderByAsc(Notice::getStatus)
                .page(page);
        return AjaxResult.success(page);

    }
    @GetMapping("/getById/{id}")
    private AjaxResult<Notice> getById(@PathVariable Long id){
        return AjaxResult.success(noticeService.getById(id));
    }
    @GetMapping("/deleteById/{id}")
    private AjaxResult<Void> deleteById(@PathVariable Long id){
        noticeService.removeById(id);
        return AjaxResult.success();
    }
    @PostMapping("/save")
    private AjaxResult<Void> save(@RequestBody Notice notice){
        noticeService.saveOrUpdate(notice);
        return AjaxResult.success();
    }
}
