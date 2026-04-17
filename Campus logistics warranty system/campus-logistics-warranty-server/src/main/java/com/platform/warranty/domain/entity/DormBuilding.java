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
 * 宿舍楼信息表
 * </p>
 *
 * @author baomidou
 * @since 2026-01-05
 */
@Getter
@Setter
@ToString
@TableName("dorm_building")
public class DormBuilding implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 楼栋编号，如 A, B1
     */
    @TableId("building_id")
    private String buildingId;

    /**
     * 楼栋名称，如 男生1号楼
     */
    @TableField("building_name")
    private String buildingName;

    /**
     * 类型：1-男生宿舍，2-女生宿舍，3-混合，4-教职工
     */
    @TableField("building_type")
    private Byte buildingType;

    /**
     * 总楼层数
     */
    @TableField("total_floors")
    private Byte totalFloors;

    /**
     * 宿管姓名
     */
    @TableField("manager_name")
    private String managerName;

    /**
     * 宿管电话
     */
    @TableField("manager_phone")
    private String managerPhone;

    /**
     * 状态：0-停用，1-启用，2-维修中
     */
    @TableField("status")
    private Byte status;

    /**
    * 楼栋图片
    */
    @TableField("img")
    private String img;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
