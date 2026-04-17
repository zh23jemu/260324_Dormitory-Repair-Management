package com.platform.warranty.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.platform.warranty.domain.dto.CommentQueryDTO;
import com.platform.warranty.domain.entity.Comment;

import java.util.List;

/**
 * <p>
 * 帖子评论表 服务类
 * </p>
 *
 * @author baomidou
 * @since 2025-05-14
 */
public interface ICommentService extends IService<Comment> {

    Page<Comment> selectAll(Page<Comment> page, CommentQueryDTO commentQueryDTO);


    List<Comment> selectAllByPostId(Integer postId);
}
