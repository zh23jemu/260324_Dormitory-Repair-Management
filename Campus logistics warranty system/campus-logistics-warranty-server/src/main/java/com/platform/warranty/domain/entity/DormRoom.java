package com.platform.warranty.domain.entity;

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
 * 宿舍房间表
 * </p>
 *
 * @author baomidou
 * @since 2026-01-05
 */
@Getter
@Setter
@ToString
@TableName("dorm_room")
public class DormRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 房间ID，如 A-201
     */
    @TableId("room_id")
    private String roomId;

    /**
     * 所属楼栋编号（逻辑外键，关联 dorm_building.building_id）
     */
    @TableField("building_id")
    private String buildingId;

    /**
     * 楼层，如 2
     */
    @TableField("floor")
    private Byte floor;

    /**
     * 房间号，如 201
     */
    @TableField("room_number")
    private String roomNumber;

    /**
     * 最大容纳人数
     */
    @TableField("max_capacity")
    private Byte maxCapacity;

    /**
     * 当前入住人数
     */
    @TableField("current_occupancy")
    private Byte currentOccupancy;

    /**
     * 房型：1-四人间，2-六人间，3-双人间，4-单人间
     */
    @TableField("room_type")
    private Byte roomType;

    /**
     * 状态：0-空闲，1-部分入住，2-已满，3-维修中
     */
    @TableField("status")
    private Byte status;

    /**
     * 备注
     */
    @TableField("remarks")
    private String remarks;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
