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
 * 用户留言表
 * </p>
 *
 * @author baomidou
 * @since 2025-05-13
 */
@Getter
@Setter
@ToString
@TableName("message")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 留言ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 留言用户ID，关联user表
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 留言内容
     */
    @TableField("content")
    private String content;
    @TableField("img")
    private String img;
    @TableField(exist = false)
    private String userName;

    /**
     * 管理员或客服的回复内容
     */
    @TableField("reply")
    private String reply;

    /**
     * 留言状态0待回复1已回复
     */
    @TableField("status")
    private Integer status;

    /**
     * 留言时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
