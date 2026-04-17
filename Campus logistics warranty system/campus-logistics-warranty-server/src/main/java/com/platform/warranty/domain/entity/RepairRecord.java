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
 * 维修记录表
 * </p>
 *
 * @author baomidou
 * @since 2025-05-14
 */
@Getter
@Setter
@ToString
@TableName("repair_record")
public class RepairRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 维修记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报修用户ID（逻辑外键到user表）
     */
    @TableField("user_id")
    private Long userId;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String workerName;
    @TableField("name")
    private String name;

    /**
     * 维修类型ID（逻辑外键到repair_type表）
     */
    @TableField("type_id")
    private Long typeId;

    /**
     * 维修地点
     */
    @TableField("location")
    private String location;

    @TableField("phone")
    private String phone;

    /**
     * 问题描述
     */
    @TableField("description")
    private String description;

    /**
     * 图片地址（可选）
     */
    @TableField("img")
    private String img;

    /**
     * 状态：待处理/处理中/已完成/已取消
     */
    @TableField("status")
    private String status;

    /**
     *
     */
    @TableField("is_charge")
    private Integer isCharge;

    @TableField("price")
    private Integer price;

    /**
     * 处理人ID（逻辑外键到user表）
     */
    @TableField("worker_id")
    private Long workerId;

    /**
     * 处理备注
     */
    @TableField("remark")
    private String remark;

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
