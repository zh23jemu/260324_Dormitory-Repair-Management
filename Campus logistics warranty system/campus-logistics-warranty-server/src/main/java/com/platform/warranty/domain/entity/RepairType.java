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
 * 维修类型表
 * </p>
 *
 * @author baomidou
 * @since 2025-05-14
 */
@Getter
@Setter
@ToString
@TableName("repair_type")
public class RepairType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 维修类型ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 维修类型名称
     */
    @TableField("name")
    private String name;

    /**
     * 描述信息
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
