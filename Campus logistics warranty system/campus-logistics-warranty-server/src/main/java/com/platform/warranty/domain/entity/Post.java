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

/**
 * <p>
 * 
 * </p>
 *
 * @author baomidou
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString
@TableName("post")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("title")
    private String title;

    @TableField("summary")
    private String summary;

    @TableField("cover")
    private String cover;

    @TableField("content")
    private String content;
    @TableField(exist = false)
    private String authorName;
    @TableField(exist = false)
    private String authorAvatar;

    /**
     * 状态0-待审核1审核通过2下架
     */
    @TableField("status")
    private Integer status;

    @TableField("author_id")
    private Long authorId;

    @TableField("views")
    private Integer views;

    @TableField("likes")
    private Integer likes;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    private LocalDateTime updatedTime;
}
