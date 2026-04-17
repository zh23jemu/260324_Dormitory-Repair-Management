package com.platform.warranty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.CommentQueryDTO;
import com.platform.warranty.domain.entity.Comment;
import com.platform.warranty.domain.entity.User;
import com.platform.warranty.service.ICommentService;
import com.platform.warranty.service.IUserService;
import com.platform.warranty.utils.AjaxResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 文章评论表 前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2025-05-05
 */
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    private final ICommentService commentService;
    private final IUserService userService;

    @GetMapping("/list")
    public AjaxResult<Page<Comment>> list(CommentQueryDTO commentQueryDTO) {
        Page<Comment> page = new Page<>(commentQueryDTO.getPageNum(), commentQueryDTO.getPageSize());
        Page<Comment> result = commentService.selectAll(page,commentQueryDTO);
        return AjaxResult.success(result);
    }

    @PostMapping("/save")
    public AjaxResult<Void> save(@RequestBody Comment comment, HttpServletRequest request) {
        Long uid = (Long) request.getAttribute("uid");
        comment.setUserId(uid);
        this.commentService.saveOrUpdate(comment);
        return AjaxResult.success();
    }

    @DeleteMapping("/delete/{id}")
    public AjaxResult<Void> delete(@PathVariable Integer id) {
        this.commentService.removeById(id);
        return AjaxResult.success();
    }

    @GetMapping("/getById/{id}")
    public AjaxResult<Comment> getById(@PathVariable Integer id) {
        return AjaxResult.success(this.commentService.getById(id));
    }

    @GetMapping("/getByPostId/{postId}")
    public AjaxResult<List<Comment>> getByPostId(@PathVariable Integer postId) {
    // 获取所有评论
    List<Comment> allComments = this.commentService.selectAllByPostId(postId);

    // 创建一个Map用于快速查找评论
    Map<Integer, Comment> commentMap = new HashMap<>();

    // 初始化每个评论，并处理用户信息和删除状态
    for (Comment comment : allComments) {
        if (comment.getIsDeleted() == 1){
            comment.setContent("该评论已被删除");
        }
        User user = userService.getById(comment.getUserId());
        if (user != null){
            comment.setUsername(user.getName());
            comment.setUserAvatar(user.getAvatar());
        }
        commentMap.put(comment.getId(), comment);
    }

    List<Comment> rootComments = new ArrayList<>();
    for (Comment comment : allComments) {
        Integer parentId = comment.getParentId();
        if (parentId == null || parentId == 0) {
            // 如果没有父ID或父ID为0，则它是根评论
            rootComments.add(comment);
        } else {
            // 查找父评论，并将其添加为子评论
            Comment parent = commentMap.get(parentId);
            if (parent != null) {
                if (parent.getReply()!= null) {
                    parent.getReply().add(comment);
                }
            }
        }
    }
    return AjaxResult.success(rootComments);
}
}
