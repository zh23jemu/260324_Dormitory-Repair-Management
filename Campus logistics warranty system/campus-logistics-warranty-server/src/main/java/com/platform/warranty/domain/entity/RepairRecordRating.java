package com.platform.warranty.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * <p>
 * 维修单评分表
 * </p>
 *
 * @author baomidou
 * @since 2025-05-14
 */
@Getter
@Setter
@ToString
@TableName("repair_record_rating")
public class RepairRecordRating implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评分ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评分用户ID，外键关联 users 表
     */
    @TableField("user_id")
    private Long userId;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String repairRecordName;

    /**
     * 商品ID，外键关联 goods 表
     */
    @TableField("repair_record_id")
    private Long repairRecordId;

    /**
     * 评分值，范围1-5
     */
    @TableField("rating")
    private Byte rating;

    /**
     * 评论内容
     */
    @TableField("comment")
    private String comment;

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
