package com.platform.warranty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.platform.warranty.domain.dto.CommentQueryDTO;
import com.platform.warranty.domain.entity.Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * <p>
 * 帖子评论表 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-05-14
 */
public interface CommentMapper extends BaseMapper<Comment> {


Page<Comment> selectAll(IPage<Comment> page, @Param("dto") CommentQueryDTO commentQueryDTO);

@Select("select * from comment where post_id = #{postId}")
List<Comment> selectAllByPostId(Integer postId);
}

