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
 * 公告表
 * </p>
 *
 * @author baomidou
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString
@TableName("notice")
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公告ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    @TableField("title")
    private String title;

    /**
     * 图片
     */
    @TableField("img")
    private String img;

    /**
     * 公告内容
     */
    @TableField("content")
    private String content;

    /**
     * 公告状态：1-已发布 2-已过期
     */
    @TableField("status")
    private Byte status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
