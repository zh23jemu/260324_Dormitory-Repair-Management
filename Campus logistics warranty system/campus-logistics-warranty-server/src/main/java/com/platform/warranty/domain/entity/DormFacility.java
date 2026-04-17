package com.platform.warranty.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * <p>
 * 宿舍物品/设施表
 * </p>
 *
 * @author baomidou
 * @since 2026-01-05
 */
@Getter
@Setter
@ToString
@TableName("dorm_facility")
public class DormFacility implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 物品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属房间ID（逻辑外键，关联 dorm_room.room_id）
     */
    @TableField("room_id")
    private String roomId;

    /**
     * 物品名称，如 空调、上下床、书桌
     */
    @TableField("facility_name")
    private String facilityName;

    /**
     * 物品类型：1-家具，2-电器，3-卫浴，4-其他
     */
    @TableField("facility_type")
    private Byte facilityType;

    /**
     * 品牌（可选）
     */
    @TableField("brand")
    private String brand;

    /**
     * 型号
     */
    @TableField("model_number")
    private String modelNumber;

    /**
     * 购置日期
     */
    @TableField("purchase_date")
    private LocalDate purchaseDate;

    /**
     * 状态：0-损坏，1-正常，2-维修中，3-报废
     */
    @TableField("status")
    private Byte status;

    /**
     * 备注，如“制冷效果差”
     */
    @TableField("remarks")
    private String remarks;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
