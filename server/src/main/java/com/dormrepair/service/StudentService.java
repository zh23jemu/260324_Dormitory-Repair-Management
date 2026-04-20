package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import com.dormrepair.dto.repair.RepairCreateRequest;
import com.dormrepair.dto.repair.RepairRatingRequest;
import com.dormrepair.dto.student.ServiceMessageRequest;
import com.dormrepair.dto.student.StudentProfileUpdateRequest;
import com.dormrepair.security.JwtUser;
import com.dormrepair.util.OrderNoUtils;
import com.dormrepair.util.SecurityUtils;
import com.dormrepair.util.TimeUtils;
import java.util.HashMap;
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
        Long facilityId = request.facilityId();
        if (buildingId == null || roomId == null) {
            Map<String, Object> profile = commonQueryService.one("select building_id, room_id from student_profile where user_id = ?", user.id());
            if (buildingId == null && profile.get("building_id") != null) {
                buildingId = ((Number) profile.get("building_id")).longValue();
            }
            if (roomId == null && profile.get("room_id") != null) {
                roomId = ((Number) profile.get("room_id")).longValue();
            }
        }
        if (facilityId != null) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from dorm_facility where id = ?", Integer.class, facilityId);
            if (count == null || count == 0) {
                throw new BusinessException("所选设施不存在");
            }
        }
        jdbcTemplate.update(
                "insert into repair_order(order_no, student_id, building_id, room_id, facility_id, repair_type_id, title, description, expect_time, status, submitted_at, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                OrderNoUtils.nextOrderNo(), user.id(), buildingId, roomId, facilityId, request.repairTypeId(), request.title(), request.description(), request.expectTime(),
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

    public List<Map<String, Object>> repairTypes() {
        SecurityUtils.requireRole("student");
        return commonQueryService.list("select id, type_name as typeName, sort_no as sortNo, status from repair_type where status = 'enabled' order by sort_no asc, id asc");
    }

    public List<Map<String, Object>> ratingIndicators() {
        SecurityUtils.requireRole("student");
        return commonQueryService.list("select id, dict_code as dictCode, dict_name as dictName, sort_no as sortNo from sys_dict where dict_type = 'rating_indicator' and status = 'enabled' order by sort_no asc, id asc");
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

    public List<Map<String, Object>> facilities(Long roomId) {
        SecurityUtils.requireRole("student");
        Long targetRoomId = roomId;
        if (targetRoomId == null) {
            Map<String, Object> profile = commonQueryService.one("select room_id from student_profile where user_id = ?", SecurityUtils.currentUser().id());
            if (profile.get("room_id") != null) {
                targetRoomId = ((Number) profile.get("room_id")).longValue();
            }
        }
        if (targetRoomId == null) {
            return List.of();
        }
        return commonQueryService.list(
                "select df.id, df.facility_name as facilityName, df.facility_type as facilityType, df.brand, df.model_number as modelNumber, df.purchase_date as purchaseDate, df.status, df.remark, dr.room_no as roomNo, db.building_name as buildingName " +
                        "from dorm_facility df left join dorm_room dr on df.room_id = dr.id left join dorm_building db on dr.building_id = db.id where df.room_id = ? order by df.id asc",
                targetRoomId
        );
    }

    public List<Map<String, Object>> repairResources() {
        return commonQueryService.list("select rr.id, rr.title, rr.category, rr.summary, rr.content, rr.cover_image as coverImage, rr.sort_no as sortNo, rr.status, rr.created_at as createdAt from repair_resource rr where rr.status = 'published' order by rr.sort_no asc, rr.id desc");
    }

    public Map<String, Object> repairResourceDetail(Long id) {
        return commonQueryService.one("select rr.id, rr.title, rr.category, rr.summary, rr.content, rr.cover_image as coverImage, rr.sort_no as sortNo, rr.status, rr.created_at as createdAt, u.real_name as publisherName from repair_resource rr left join user u on rr.publisher_id = u.id where rr.id = ? and rr.status = 'published'", id);
    }

    public List<Map<String, Object>> repairers() {
        return commonQueryService.list(
                "select u.id, u.real_name as realName, u.phone, u.work_type_code as workTypeCode, d.dict_name as workTypeName, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id) as totalCount, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id and ro.status in ('pending_rating', 'completed')) as completedCount, " +
                        "(select round(avg(rr.score), 2) from repair_rating rr left join repair_order ro on rr.repair_order_id = ro.id where ro.assigned_repairer_id = u.id) as avgScore " +
                        "from user u left join sys_dict d on u.work_type_code = d.dict_code and d.dict_type = 'repair_work_type' where u.role = 'repairer' and u.status = 'enabled' order by u.id asc"
        );
    }

    public Map<String, Object> repairerDetail(Long id) {
        Map<String, Object> detail = commonQueryService.one(
                "select u.id, u.username, u.real_name as realName, u.phone, u.work_type_code as workTypeCode, d.dict_name as workTypeName, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id) as totalCount, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id and ro.status in ('pending_rating', 'completed')) as completedCount, " +
                        "(select round(avg(rr.score), 2) from repair_rating rr left join repair_order ro on rr.repair_order_id = ro.id where ro.assigned_repairer_id = u.id) as avgScore " +
                        "from user u left join sys_dict d on u.work_type_code = d.dict_code and d.dict_type = 'repair_work_type' where u.role = 'repairer' and u.id = ?",
                id
        );
        int total = detail.get("totalCount") == null ? 0 : ((Number) detail.get("totalCount")).intValue();
        int completed = detail.get("completedCount") == null ? 0 : ((Number) detail.get("completedCount")).intValue();
        detail.put("completionRate", total == 0 ? 0 : Math.round(completed * 10000.0 / total) / 100.0);
        detail.put("recentOrders", commonQueryService.list(
                "select ro.id, ro.order_no as orderNo, ro.title, ro.status, ro.completed_at as completedAt, rt.type_name as repairTypeName " +
                        "from repair_order ro left join repair_type rt on ro.repair_type_id = rt.id where ro.assigned_repairer_id = ? order by ro.id desc limit 5",
                id
        ));
        return detail;
    }

    public List<Map<String, Object>> serviceMessages() {
        SecurityUtils.requireRole("student");
        return commonQueryService.list(
                "select sm.id, sm.title, sm.content, sm.image_path as imagePath, sm.status, sm.reply_content as replyContent, sm.replied_at as repliedAt, sm.created_at as createdAt " +
                        "from service_message sm where sm.student_id = ? order by sm.id desc",
                SecurityUtils.currentUser().id()
        );
    }

    public void createServiceMessage(ServiceMessageRequest request) {
        SecurityUtils.requireRole("student");
        JwtUser user = SecurityUtils.currentUser();
        String now = TimeUtils.now();
        jdbcTemplate.update(
                "insert into service_message(student_id, title, content, image_path, status, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?)",
                user.id(), request.title(), request.content(), request.imagePath(), "pending", now, now
        );
        logService.log(user.id(), "服务留言", "新增", "提交服务留言: " + request.title());
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
        Map<String, Object> profile = commonQueryService.one(
                "select u.id, u.username, u.real_name as realName, u.phone, sp.student_no as studentNo, sp.college, sp.major, sp.class_name as className, sp.building_id as buildingId, sp.room_id as roomId, sp.bed_no as bedNo, db.building_name as buildingName, dr.room_no as roomNo " +
                        "from user u left join student_profile sp on u.id = sp.user_id left join dorm_building db on sp.building_id = db.id left join dorm_room dr on sp.room_id = dr.id where u.id = ?",
                SecurityUtils.currentUser().id()
        );
        profile.put("serviceStats", homeSummary());
        return profile;
    }

    public void updateProfile(StudentProfileUpdateRequest request) {
        SecurityUtils.requireRole("student");
        JwtUser user = SecurityUtils.currentUser();
        String now = TimeUtils.now();
        jdbcTemplate.update("update user set phone = ?, updated_at = ? where id = ?", request.phone(), now, user.id());
        jdbcTemplate.update("update student_profile set building_id = ?, room_id = ?, bed_no = ?, updated_at = ? where user_id = ?", request.buildingId(), request.roomId(), request.bedNo(), now, user.id());
        logService.log(user.id(), "个人信息", "修改", "更新学生个人信息");
    }

    public Map<String, Object> homeSummary() {
        SecurityUtils.requireRole("student");
        JwtUser user = SecurityUtils.currentUser();
        Map<String, Object> map = new HashMap<>();
        map.put("announcementCount", jdbcTemplate.queryForObject("select count(*) from announcement where status = 'published'", Integer.class));
        map.put("resourceCount", jdbcTemplate.queryForObject("select count(*) from repair_resource where status = 'published'", Integer.class));
        map.put("myOrderCount", jdbcTemplate.queryForObject("select count(*) from repair_order where student_id = ?", Integer.class, user.id()));
        map.put("pendingCount", jdbcTemplate.queryForObject("select count(*) from repair_order where student_id = ? and status not in ('completed', 'rejected')", Integer.class, user.id()));
        map.put("messageCount", jdbcTemplate.queryForObject("select count(*) from service_message where student_id = ?", Integer.class, user.id()));
        map.put("latestResources", commonQueryService.list("select id, title, category from repair_resource where status = 'published' order by sort_no asc, id desc limit 3"));
        map.put("latestOrders", commonQueryService.list(
                "select ro.id, ro.order_no as orderNo, ro.title, ro.status, ro.created_at as createdAt from repair_order ro where ro.student_id = ? order by ro.id desc limit 3",
                user.id()
        ));
        return map;
    }

    private String baseRepairOrderSql() {
        return "select ro.id, ro.order_no as orderNo, ro.title, ro.description, ro.expect_time as expectTime, ro.status, ro.reject_reason as rejectReason, ro.submitted_at as submittedAt, ro.assigned_at as assignedAt, ro.completed_at as completedAt, rt.type_name as repairTypeName, db.building_name as buildingName, dr.room_no as roomNo, u.real_name as studentName, ru.real_name as repairerName, rf.result_desc as resultDesc, rf.materials_used as materialsUsed, df.id as facilityId, df.facility_name as facilityName, df.facility_type as facilityType from repair_order ro left join repair_type rt on ro.repair_type_id = rt.id left join dorm_building db on ro.building_id = db.id left join dorm_room dr on ro.room_id = dr.id left join dorm_facility df on ro.facility_id = df.id left join user u on ro.student_id = u.id left join user ru on ro.assigned_repairer_id = ru.id left join repair_feedback rf on ro.id = rf.repair_order_id";
    }
}
