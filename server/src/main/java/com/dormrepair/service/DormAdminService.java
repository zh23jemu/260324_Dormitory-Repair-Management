package com.dormrepair.service;

import com.dormrepair.dto.admin.AnnouncementRequest;
import com.dormrepair.dto.admin.BuildingRequest;
import com.dormrepair.dto.admin.RoomRequest;
import com.dormrepair.dto.admin.StudentRoomRequest;
import com.dormrepair.dto.repair.RepairAssignRequest;
import com.dormrepair.dto.repair.RepairRejectRequest;
import com.dormrepair.security.JwtUser;
import com.dormrepair.util.SecurityUtils;
import com.dormrepair.util.TimeUtils;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DormAdminService {

    private final JdbcTemplate jdbcTemplate;
    private final CommonQueryService commonQueryService;
    private final LogService logService;

    public DormAdminService(JdbcTemplate jdbcTemplate, CommonQueryService commonQueryService, LogService logService) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonQueryService = commonQueryService;
        this.logService = logService;
    }

    public List<Map<String, Object>> repairOrders(String status) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        String sql = baseOrderSql() + " where 1=1";
        if (status != null && !status.isBlank()) {
            sql += " and ro.status = '" + status + "'";
        }
        sql += " order by ro.id desc";
        return commonQueryService.list(sql);
    }

    public List<Map<String, Object>> pendingReviewOrders() {
        return repairOrders("pending_review");
    }

    public Map<String, Object> repairOrderDetail(Long id) {
        Map<String, Object> detail = commonQueryService.one(baseOrderSql() + " where ro.id = ?", id);
        detail.put("images", commonQueryService.list("select id, image_type, file_path, created_at from repair_order_image where repair_order_id = ? order by id asc", id));
        detail.put("flows", commonQueryService.list("select rf.id, rf.from_status, rf.to_status, rf.remark, rf.created_at, u.real_name as operator_name from repair_flow rf left join user u on rf.operator_id = u.id where rf.repair_order_id = ? order by rf.id asc", id));
        List<Map<String, Object>> ratings = commonQueryService.list("select score, content, created_at from repair_rating where repair_order_id = ?", id);
        detail.put("rating", ratings.isEmpty() ? null : ratings.get(0));
        return detail;
    }

    public void assign(Long id, RepairAssignRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        JwtUser user = SecurityUtils.currentUser();
        String now = TimeUtils.now();
        jdbcTemplate.update("update repair_order set assigned_repairer_id = ?, status = ?, assigned_at = ?, updated_at = ? where id = ?", request.repairerId(), "pending_accept", now, now, id);
        jdbcTemplate.update("insert into repair_flow(repair_order_id, operator_id, from_status, to_status, remark, created_at) values (?, ?, ?, ?, ?, ?)", id, user.id(), "pending_review", "pending_accept", request.remark() == null ? "宿管分配维修人员" : request.remark(), now);
        logService.log(user.id(), "工单管理", "分配", "分配工单给维修人员: " + id);
    }

    public void reject(Long id, RepairRejectRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        JwtUser user = SecurityUtils.currentUser();
        String now = TimeUtils.now();
        jdbcTemplate.update("update repair_order set status = ?, reject_reason = ?, updated_at = ? where id = ?", "rejected", request.rejectReason(), now, id);
        jdbcTemplate.update("insert into repair_flow(repair_order_id, operator_id, from_status, to_status, remark, created_at) values (?, ?, ?, ?, ?, ?)", id, user.id(), "pending_review", "rejected", request.rejectReason(), now);
        logService.log(user.id(), "工单管理", "驳回", "驳回工单: " + id);
    }

    public List<Map<String, Object>> repairers() {
        return commonQueryService.list("select id, username, real_name as realName, phone, status from user where role = 'repairer' order by id asc");
    }

    public List<Map<String, Object>> buildings() {
        return commonQueryService.list("select id, building_name as buildingName, building_code as buildingCode, gender_type as genderType, floor_count as floorCount, remark from dorm_building order by id asc");
    }

    public void addBuilding(BuildingRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update("insert into dorm_building(building_name, building_code, gender_type, floor_count, remark) values (?, ?, ?, ?, ?)", request.buildingName(), request.buildingCode(), request.genderType(), request.floorCount(), request.remark());
    }

    public void updateBuilding(Long id, BuildingRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update("update dorm_building set building_name = ?, building_code = ?, gender_type = ?, floor_count = ?, remark = ? where id = ?", request.buildingName(), request.buildingCode(), request.genderType(), request.floorCount(), request.remark(), id);
    }

    public List<Map<String, Object>> rooms() {
        return commonQueryService.list("select dr.id, dr.room_no as roomNo, dr.capacity, dr.current_count as currentCount, dr.facility_desc as facilityDesc, dr.status, dr.remark, db.id as buildingId, db.building_name as buildingName from dorm_room dr left join dorm_building db on dr.building_id = db.id order by dr.id asc");
    }

    public void addRoom(RoomRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update("insert into dorm_room(building_id, room_no, capacity, current_count, facility_desc, status, remark) values (?, ?, ?, ?, ?, ?, ?)", request.buildingId(), request.roomNo(), request.capacity(), 0, request.facilityDesc(), request.status() == null ? "enabled" : request.status(), request.remark());
    }

    public void updateRoom(Long id, RoomRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update("update dorm_room set building_id = ?, room_no = ?, capacity = ?, facility_desc = ?, status = ?, remark = ? where id = ?", request.buildingId(), request.roomNo(), request.capacity(), request.facilityDesc(), request.status() == null ? "enabled" : request.status(), request.remark(), id);
    }

    public List<Map<String, Object>> students() {
        return commonQueryService.list("select sp.id, sp.student_no as studentNo, u.real_name as realName, u.phone, sp.college, sp.major, sp.class_name as className, sp.building_id as buildingId, sp.room_id as roomId, sp.bed_no as bedNo, db.building_name as buildingName, dr.room_no as roomNo from student_profile sp left join user u on sp.user_id = u.id left join dorm_building db on sp.building_id = db.id left join dorm_room dr on sp.room_id = dr.id order by sp.id asc");
    }

    public void updateStudentRoom(Long id, StudentRoomRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update("update student_profile set building_id = ?, room_id = ?, bed_no = ?, updated_at = ? where id = ?", request.buildingId(), request.roomId(), request.bedNo(), TimeUtils.now(), id);
    }

    public List<Map<String, Object>> announcements() {
        return commonQueryService.list("select a.id, a.title, a.content, a.status, a.published_at as publishedAt, a.created_at as createdAt, u.real_name as publisherName from announcement a left join user u on a.publisher_id = u.id order by a.id desc");
    }

    public void createAnnouncement(AnnouncementRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        JwtUser user = SecurityUtils.currentUser();
        String now = TimeUtils.now();
        jdbcTemplate.update("insert into announcement(title, content, publisher_id, status, published_at, created_at) values (?, ?, ?, ?, ?, ?)", request.title(), request.content(), user.id(), request.status() == null ? "published" : request.status(), now, now);
        logService.log(user.id(), "公告管理", "新增", "发布公告: " + request.title());
    }

    public void updateAnnouncement(Long id, AnnouncementRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update("update announcement set title = ?, content = ?, status = ?, published_at = ? where id = ?", request.title(), request.content(), request.status() == null ? "published" : request.status(), TimeUtils.now(), id);
    }

    public void deleteAnnouncement(Long id) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update("delete from announcement where id = ?", id);
    }

    private String baseOrderSql() {
        return "select ro.id, ro.order_no as orderNo, ro.title, ro.description, ro.status, ro.expect_time as expectTime, ro.reject_reason as rejectReason, ro.submitted_at as submittedAt, ro.assigned_at as assignedAt, ro.completed_at as completedAt, rt.type_name as repairTypeName, db.building_name as buildingName, dr.room_no as roomNo, su.real_name as studentName, ru.real_name as repairerName from repair_order ro left join repair_type rt on ro.repair_type_id = rt.id left join dorm_building db on ro.building_id = db.id left join dorm_room dr on ro.room_id = dr.id left join user su on ro.student_id = su.id left join user ru on ro.assigned_repairer_id = ru.id";
    }
}
