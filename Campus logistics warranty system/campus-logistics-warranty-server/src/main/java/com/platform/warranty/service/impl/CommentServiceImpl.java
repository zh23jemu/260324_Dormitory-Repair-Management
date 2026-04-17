package com.platform.warranty.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.platform.warranty.domain.dto.CommentQueryDTO;
import com.platform.warranty.domain.entity.Comment;
import com.platform.warranty.mapper.CommentMapper;
import com.platform.warranty.service.ICommentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 帖子评论表 服务实现类
 * </p>
 *
 * @author baomidou
 * @since 2025-05-14
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {


    @Override
    public Page<Comment> selectAll(Page<Comment> page, CommentQueryDTO commentQueryDTO) {
        return this.getBaseMapper().selectAll(page,commentQueryDTO);
    }

    @Override
    public List<Comment> selectAllByPostId(Integer postId) {
        return this.getBaseMapper().selectAllByPostId(postId);
    }
}
