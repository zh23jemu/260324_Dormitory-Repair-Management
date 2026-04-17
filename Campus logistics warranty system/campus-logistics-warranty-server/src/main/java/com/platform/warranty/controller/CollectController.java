package com.platform.warranty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.CollectQueryDTO;
import com.platform.warranty.domain.entity.Collect;
import com.platform.warranty.domain.entity.Post;
import com.platform.warranty.service.ICollectService;
import com.platform.warranty.service.IPostService;
import com.platform.warranty.utils.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.platform.warranty.utils.CommonUtils.getUserId;

/**
 * <p>
 * 用户收藏表 前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2025-05-15
 */
@RestController
@RequestMapping("/collect")
@RequiredArgsConstructor
public class CollectController {

    private final ICollectService collectService;
    private final IPostService   postService;
    @GetMapping("/list")
    public AjaxResult<Page<Collect>> list(CollectQueryDTO collectQueryDTO, HttpServletRequest request) {
        Long userId = getUserId(request);
        Page<Collect> page = new Page<>(collectQueryDTO.getPageNum(), collectQueryDTO.getPageSize());
        collectService.lambdaQuery()
                .eq(Collect::getUserId, userId)
                .page(page);
        for (Collect collect : page.getRecords()) {
            Long postId = collect.getPostId();
            Post byId = postService.getById(postId);
            if (byId != null){
                collect.setPostTitle(byId.getTitle());
            }
        }
        return AjaxResult.success(page);
    }
    @PostMapping("/save")
    public AjaxResult<Void> save(@RequestBody Collect collect, HttpServletRequest request) {
        Long userId1 = collect.getUserId();
        Long postId = collect.getPostId();
        Collect one = collectService.lambdaQuery()
                .eq(Collect::getUserId, userId1)
                .eq(Collect::getPostId, postId)
                .one();
        if (one != null) {
            return AjaxResult.error("您已收藏过");
        }
        Long userId = getUserId(request);
        collect.setUserId(userId);
        collectService.saveOrUpdate(collect);
        return AjaxResult.success();
    }
    @DeleteMapping("/deleteById/{id}")
    public AjaxResult<Void> deleteById(@PathVariable("id") Long id) {
        collectService.removeById(id);
        return AjaxResult.success();
    }
}
