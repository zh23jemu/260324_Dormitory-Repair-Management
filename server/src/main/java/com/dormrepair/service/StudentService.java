package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import com.dormrepair.dto.admin.StudentRoomRequest;
import com.dormrepair.dto.repair.RepairCreateRequest;
import com.dormrepair.dto.repair.RepairRatingRequest;
import com.dormrepair.security.JwtUser;
import com.dormrepair.util.OrderNoUtils;
import com.dormrepair.util.SecurityUtils;
import com.dormrepair.util.TimeUtils;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final JdbcTemplate jdbcTemplate;
    private final CommonQueryService commonQueryService;
    private final LogService logService;

    public StudentService(JdbcTemplate jdbcTemplate, CommonQueryService commonQueryService, LogService logService) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonQueryService = commonQueryService;
        this.logService = logService;
    }

    public void createRepairOrder(RepairCreateRequest request) {
        SecurityUtils.requireRole("student");
        JwtUser user = SecurityUtils.currentUser();
        String now = TimeUtils.now();
        Long buildingId = request.buildingId();
        Long roomId = request.roomId();
        if (buildingId == null || roomId == null) {
            Map<String, Object> profile = commonQueryService.one("select building_id, room_id from student_profile where user_id = ?", user.id());
            if (buildingId == null && profile.get("building_id") != null) {
                buildingId = ((Number) profile.get("building_id")).longValue();
            }
            if (roomId == null && profile.get("room_id") != null) {
                roomId = ((Number) profile.get("room_id")).longValue();
            }
        }
        jdbcTemplate.update(
                "insert into repair_order(order_no, student_id, building_id, room_id, repair_type_id, title, description, expect_time, status, submitted_at, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                OrderNoUtils.nextOrderNo(), user.id(), buildingId, roomId, request.repairTypeId(), request.title(), request.description(), request.expectTime(),
                "pending_review", now, now, now
        );
        Long orderId = jdbcTemplate.queryForObject("select last_insert_rowid()", Long.class);
        if (request.imagePaths() != null) {
            for (String imagePath : request.imagePaths()) {
                jdbcTemplate.update("insert into repair_order_image(repair_order_id, image_type, file_path, created_at) values (?, ?, ?, ?)", orderId, "fault", imagePath, now);
            }
        }
        jdbcTemplate.update("insert into repair_flow(repair_order_id, operator_id, from_status, to_status, remark, created_at) values (?, ?, ?, ?, ?, ?)",
                orderId, user.id(), null, "pending_review", "学生提交报修申请", now);
        logService.log(user.id(), "报修申请", "新增", "提交报修工单: " + orderId);
    }

    public List<Map<String, Object>> myRepairOrders(String status) {
        SecurityUtils.requireRole("student");
        JwtUser user = SecurityUtils.currentUser();
        String sql = baseRepairOrderSql() + " where ro.student_id = ?";
        if (status != null && !status.isBlank()) {
            sql += " and ro.status = '" + status + "'";
        }
        sql += " order by ro.id desc";
        return commonQueryService.list(sql, user.id());
    }

    public Map<String, Object> myRepairOrderDetail(Long id) {
        SecurityUtils.requireRole("student");
        JwtUser user = SecurityUtils.currentUser();
        Map<String, Object> detail = commonQueryService.one(baseRepairOrderSql() + " where ro.id = ? and ro.student_id = ?", id, user.id());
        detail.put("images", commonQueryService.list("select id, image_type, file_path, created_at from repair_order_image where repair_order_id = ? order by id asc", id));
        detail.put("flows", commonQueryService.list("select rf.id, rf.from_status, rf.to_status, rf.remark, rf.created_at, u.real_name as operator_name from repair_flow rf left join user u on rf.operator_id = u.id where rf.repair_order_id = ? order by rf.id asc", id));
        List<Map<String, Object>> ratings = commonQueryService.list("select score, content, created_at from repair_rating where repair_order_id = ?", id);
        detail.put("rating", ratings.isEmpty() ? null : ratings.get(0));
        return detail;
    }

    public void rateRepairOrder(Long id, RepairRatingRequest request) {
        SecurityUtils.requireRole("student");
        JwtUser user = SecurityUtils.currentUser();
        Map<String, Object> order = commonQueryService.one("select id, status, student_id from repair_order where id = ?", id);
        if (((Number) order.get("student_id")).longValue() != user.id()) {
            throw new BusinessException("无权限评价该工单");
        }
        if (!"pending_rating".equals(order.get("status"))) {
            throw new BusinessException("当前工单不能评价");
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from repair_rating where repair_order_id = ?", Integer.class, id);
        if (count != null && count > 0) {
            throw new BusinessException("该工单已评价");
        }
        String now = TimeUtils.now();
        jdbcTemplate.update("insert into repair_rating(repair_order_id, student_id, score, content, created_at) values (?, ?, ?, ?, ?)", id, user.id(), request.score(), request.content(), now);
        jdbcTemplate.update("update repair_order set status = ?, updated_at = ? where id = ?", "completed", now, id);
        jdbcTemplate.update("insert into repair_flow(repair_order_id, operator_id, from_status, to_status, remark, created_at) values (?, ?, ?, ?, ?, ?)",
                id, user.id(), "pending_rating", "completed", "学生完成评价", now);
        logService.log(user.id(), "工单评价", "新增", "评价工单: " + id);
    }

    public Map<String, Object> myProfile() {
        SecurityUtils.requireRole("student");
        return commonQueryService.one("select u.id, u.username, u.real_name as realName, u.phone, sp.student_no as studentNo, sp.college, sp.major, sp.class_name as className, sp.building_id as buildingId, sp.room_id as roomId, sp.bed_no as bedNo from user u left join student_profile sp on u.id = sp.user_id where u.id = ?", SecurityUtils.currentUser().id());
    }

    public void updateProfile(StudentRoomRequest request) {
        SecurityUtils.requireRole("student");
        JwtUser user = SecurityUtils.currentUser();
        jdbcTemplate.update("update student_profile set building_id = ?, room_id = ?, bed_no = ?, updated_at = ? where user_id = ?", request.buildingId(), request.roomId(), request.bedNo(), TimeUtils.now(), user.id());
        logService.log(user.id(), "个人信息", "修改", "更新学生宿舍信息");
    }

    private String baseRepairOrderSql() {
        return "select ro.id, ro.order_no as orderNo, ro.title, ro.description, ro.expect_time as expectTime, ro.status, ro.reject_reason as rejectReason, ro.submitted_at as submittedAt, ro.assigned_at as assignedAt, ro.completed_at as completedAt, rt.type_name as repairTypeName, db.building_name as buildingName, dr.room_no as roomNo, u.real_name as studentName, ru.real_name as repairerName, rf.result_desc as resultDesc, rf.materials_used as materialsUsed from repair_order ro left join repair_type rt on ro.repair_type_id = rt.id left join dorm_building db on ro.building_id = db.id left join dorm_room dr on ro.room_id = dr.id left join user u on ro.student_id = u.id left join user ru on ro.assigned_repairer_id = ru.id left join repair_feedback rf on ro.id = rf.repair_order_id";
    }
}
