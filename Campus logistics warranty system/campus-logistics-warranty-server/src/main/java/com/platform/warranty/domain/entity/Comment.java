package com.platform.warranty.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 帖子评论表
 * </p>
 *
 * @author baomidou
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString
@TableName("comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 关联文章ID
     */
    @TableField("post_id")
    private Integer postId;



    @TableField(exist = false)
    private String userAvatar;
    @TableField(exist = false)
    private String username;
    /**
     * 评论用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 点赞数量
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 父评论ID（支持回复功能）
     */
    @TableField("parent_id")
    private Integer parentId;

    /**
     * 0-正常 1-删除
     */
    @TableField("is_deleted")
    private Byte isDeleted;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

      @TableField(exist = false)
    private List<Comment> reply = new ArrayList<>();
}
