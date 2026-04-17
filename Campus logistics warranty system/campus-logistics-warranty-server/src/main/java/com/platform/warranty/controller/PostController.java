package com.platform.warranty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.PostDTO;
import com.platform.warranty.domain.entity.Post;
import com.platform.warranty.domain.entity.User;
import com.platform.warranty.service.IPostService;
import com.platform.warranty.service.IUserService;
import com.platform.warranty.utils.AjaxResult;
import com.platform.warranty.utils.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2025-05-13
 */
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final IPostService postService;
    private final IUserService userService;

    @GetMapping("/list")
    public AjaxResult<Page<Post>> list(PostDTO postDTO, HttpServletRequest request) {
        Page<Post> page = new Page<>(postDTO.getPageNum(), postDTO.getPageSize());
        Long userId = CommonUtils.getUserId(request);
        User user = userService.getById(userId);
        if (user.getRole().equalsIgnoreCase("admin")) {
            postService.lambdaQuery()
                    .like(StringUtils.isNotBlank(postDTO.getKeyword()), Post::getTitle, postDTO.getKeyword())
                    .or()
                    .like(StringUtils.isNotBlank(postDTO.getKeyword()), Post::getSummary, postDTO.getKeyword())
                    .eq(postDTO.getStatus() != null, Post::getStatus, postDTO.getStatus()).page(page);
            for (Post record : page.getRecords()) {

                Long authorId = record.getAuthorId();
                User user2 = userService.getById(authorId);
                if (user2 != null) {
                    record.setAuthorName(user2.getName());
                    record.setAuthorAvatar(user2.getAvatar());
                }
            }

            return getPageAjaxResult(page);
        }
        postService.lambdaQuery()
                .like(StringUtils.isNotBlank(postDTO.getKeyword()), Post::getTitle, postDTO.getKeyword())
                .or()
                .like(StringUtils.isNotBlank(postDTO.getKeyword()), Post::getSummary, postDTO.getKeyword())
                .eq(Post::getStatus, 1)
                .page(page);
        for (Post record : page.getRecords()) {
                Long authorId = record.getAuthorId();
                User user2 = userService.getById(authorId);
                if (user2 != null) {
                    record.setAuthorName(user2.getName());
                    record.setAuthorAvatar(user2.getAvatar());
                }
            }
        return getPageAjaxResult(page);
    }

    private AjaxResult<Page<Post>> getPageAjaxResult(Page<Post> page) {
        page.getRecords().forEach(post -> {
            Long authorId = post.getAuthorId();
            User user2 = userService.getById(authorId);
            if (user2 != null) {
                post.setAuthorName(user2.getName());
                post.setAuthorAvatar(user2.getAvatar());
            }
        });
        return AjaxResult.success(page);
    }

    @GetMapping("/getById/{id}")
    public AjaxResult<Post> getById(@PathVariable("id") Long id) {
        Post post = postService.getById(id);
        post.setViews(post.getViews() + 1);
        postService.updateById(post);
        return AjaxResult.success(post);
    }

    @DeleteMapping("/deleteById/{id}")
    public AjaxResult<Void> deleteById(@PathVariable("id") Long id) {
        postService.removeById(id);
        return AjaxResult.success();
    }

    @PostMapping("/save")
    public AjaxResult<Void> save(@RequestBody Post post,HttpServletRequest request) {
        Long authorId = post.getAuthorId();
        if (authorId == null){
            post.setAuthorId(CommonUtils.getUserId(request));
        }
        postService.saveOrUpdate(post);
        return AjaxResult.success();
    }
}
