package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import com.dormrepair.dto.admin.AnnouncementRequest;
import com.dormrepair.dto.admin.BuildingRequest;
import com.dormrepair.dto.admin.FacilityRequest;
import com.dormrepair.dto.admin.RoomRequest;
import com.dormrepair.dto.admin.StudentRoomRequest;
import com.dormrepair.dto.repair.RepairOrderQueryRequest;
import com.dormrepair.dto.repair.RepairAssignRequest;
import com.dormrepair.dto.repair.RepairRejectRequest;
import com.dormrepair.security.JwtUser;
import com.dormrepair.util.SecurityUtils;
import com.dormrepair.util.TimeUtils;
import java.util.ArrayList;
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

    public List<Map<String, Object>> repairOrders(RepairOrderQueryRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        StringBuilder sql = new StringBuilder(baseOrderSql()).append(" where 1=1");
        List<Object> args = new ArrayList<>();
        if (request != null) {
            if (request.keyword() != null && !request.keyword().isBlank()) {
                sql.append(" and (ro.order_no like ? or ro.title like ? or su.real_name like ? or dr.room_no like ?)");
                String keyword = "%" + request.keyword().trim() + "%";
                args.add(keyword);
                args.add(keyword);
                args.add(keyword);
                args.add(keyword);
            }
            if (request.status() != null && !request.status().isBlank()) {
                sql.append(" and ro.status = ?");
                args.add(request.status());
            }
            if (request.repairTypeId() != null) {
                sql.append(" and ro.repair_type_id = ?");
                args.add(request.repairTypeId());
            }
            if (request.buildingId() != null) {
                sql.append(" and ro.building_id = ?");
                args.add(request.buildingId());
            }
            if (request.repairerId() != null) {
                sql.append(" and ro.assigned_repairer_id = ?");
                args.add(request.repairerId());
            }
            if (request.dateFrom() != null && !request.dateFrom().isBlank()) {
                sql.append(" and ro.submitted_at >= ?");
                args.add(request.dateFrom() + " 00:00:00");
            }
            if (request.dateTo() != null && !request.dateTo().isBlank()) {
                sql.append(" and ro.submitted_at <= ?");
                args.add(request.dateTo() + " 23:59:59");
            }
        }
        sql.append(" order by ro.id desc");
        return commonQueryService.list(sql.toString(), args.toArray());
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
        return commonQueryService.list(
                "select u.id, u.username, u.real_name as realName, u.phone, u.status, u.work_type_code as workTypeCode, (select group_concat(d.dict_name, '、') from sys_dict d where d.dict_type = 'repair_work_type' and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || d.dict_code || ',') > 0) as workTypeName, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id) as totalCount, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id and ro.status in ('pending_rating', 'completed')) as completedCount, " +
                        "(select round(avg(rr.score), 2) from repair_rating rr left join repair_order ro on rr.repair_order_id = ro.id where ro.assigned_repairer_id = u.id) as avgScore " +
                        "from user u where u.role = 'repairer' order by u.id asc"
        );
    }

    /**
     * 查询宿管侧可见的报修类型列表。
     * 这里只提供读取能力，供工单筛选和分配场景使用；
     * 真正的报修类型增删改仍然保留在管理员模块中，避免角色边界混乱。
     *
     * @return 报修类型列表，按排序号和主键升序返回
     */
    public List<Map<String, Object>> repairTypes() {
        SecurityUtils.requireRole("dorm_admin", "admin");
        return commonQueryService.list("select id, type_name as typeName, sort_no as sortNo, status from repair_type order by sort_no asc, id asc");
    }

    public List<Map<String, Object>> recommendedRepairers(Long repairTypeId) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        String workTypeCode = null;
        if (repairTypeId != null) {
            Map<String, Object> type = commonQueryService.one("select type_name as typeName from repair_type where id = ?", repairTypeId);
            String typeName = String.valueOf(type.get("typeName"));
            if (typeName.contains("水")) workTypeCode = "electrician";
            else if (typeName.contains("家具") || typeName.contains("门")) workTypeCode = "carpenter";
            else if (typeName.contains("网")) workTypeCode = "network";
        }
        if (workTypeCode == null) {
            return repairers();
        }
        return commonQueryService.list(
                "select u.id, u.username, u.real_name as realName, u.phone, u.status, u.work_type_code as workTypeCode, " +
                        "(select group_concat(d.dict_name, '、') from sys_dict d where d.dict_type = 'repair_work_type' and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || d.dict_code || ',') > 0) as workTypeName, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id) as totalCount, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id and ro.status in ('pending_rating', 'completed')) as completedCount, " +
                        "(select round(avg(rr.score), 2) from repair_rating rr left join repair_order ro on rr.repair_order_id = ro.id where ro.assigned_repairer_id = u.id) as avgScore " +
                        "from user u where u.role = 'repairer' and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || ? || ',') > 0 order by u.id asc",
                workTypeCode
        );
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

    /**
     * 删除楼栋前先检查是否还有宿舍引用。
     * 楼栋属于宿舍和学生住宿关系的上级数据，直接删除会导致页面无法正确展示位置，
     * 因此这里只允许删除未被任何宿舍使用的楼栋。
     */
    public void deleteBuilding(Long id) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        Integer roomCount = jdbcTemplate.queryForObject("select count(*) from dorm_room where building_id = ?", Integer.class, id);
        if (roomCount != null && roomCount > 0) {
            throw new BusinessException("该楼栋下仍有宿舍，不能删除");
        }
        jdbcTemplate.update("delete from dorm_building where id = ?", id);
        logService.log(SecurityUtils.currentUser().id(), "楼栋宿舍", "删除", "删除楼栋: " + id);
    }

    public List<Map<String, Object>> rooms() {
        return commonQueryService.list("select dr.id, dr.room_no as roomNo, dr.capacity, dr.current_count as currentCount, dr.facility_desc as facilityDesc, dr.status, dr.remark, db.id as buildingId, db.building_name as buildingName from dorm_room dr left join dorm_building db on dr.building_id = db.id order by dr.id asc");
    }

    public List<Map<String, Object>> facilities(Long roomId, String keyword, String status) {
        StringBuilder sql = new StringBuilder(
                "select df.id, df.room_id as roomId, df.facility_name as facilityName, df.facility_type as facilityType, df.brand, df.model_number as modelNumber, df.purchase_date as purchaseDate, df.status, df.remark, df.created_at as createdAt, dr.room_no as roomNo, db.building_name as buildingName " +
                        "from dorm_facility df left join dorm_room dr on df.room_id = dr.id left join dorm_building db on dr.building_id = db.id where 1=1"
        );
        List<Object> args = new ArrayList<>();
        if (roomId != null) {
            sql.append(" and df.room_id = ?");
            args.add(roomId);
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" and (df.facility_name like ? or df.facility_type like ? or dr.room_no like ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
            args.add(like);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" and df.status = ?");
            args.add(status);
        }
        sql.append(" order by df.id desc");
        return commonQueryService.list(sql.toString(), args.toArray());
    }

    public void createFacility(FacilityRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        String now = TimeUtils.now();
        jdbcTemplate.update(
                "insert into dorm_facility(room_id, facility_name, facility_type, brand, model_number, purchase_date, status, remark, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                request.roomId(), request.facilityName(), request.facilityType(), request.brand(), request.modelNumber(), request.purchaseDate(),
                request.status() == null ? "normal" : request.status(), request.remark(), now, now
        );
        logService.log(SecurityUtils.currentUser().id(), "设施台账", "新增", "新增宿舍设施: " + request.facilityName());
    }

    public void updateFacility(Long id, FacilityRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update(
                "update dorm_facility set room_id = ?, facility_name = ?, facility_type = ?, brand = ?, model_number = ?, purchase_date = ?, status = ?, remark = ?, updated_at = ? where id = ?",
                request.roomId(), request.facilityName(), request.facilityType(), request.brand(), request.modelNumber(), request.purchaseDate(),
                request.status() == null ? "normal" : request.status(), request.remark(), TimeUtils.now(), id
        );
        logService.log(SecurityUtils.currentUser().id(), "设施台账", "修改", "修改宿舍设施: " + id);
    }

    public void deleteFacility(Long id) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        Integer count = jdbcTemplate.queryForObject("select count(*) from repair_order where facility_id = ? and status not in ('completed', 'rejected')", Integer.class, id);
        if (count != null && count > 0) {
            throw new BusinessException("该设施存在未完成关联工单，不能删除");
        }
        jdbcTemplate.update("delete from dorm_facility where id = ?", id);
        logService.log(SecurityUtils.currentUser().id(), "设施台账", "删除", "删除宿舍设施: " + id);
    }

    public void addRoom(RoomRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update("insert into dorm_room(building_id, room_no, capacity, current_count, facility_desc, status, remark) values (?, ?, ?, ?, ?, ?, ?)", request.buildingId(), request.roomNo(), request.capacity(), 0, request.facilityDesc(), request.status() == null ? "enabled" : request.status(), request.remark());
    }

    public void updateRoom(Long id, RoomRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update("update dorm_room set building_id = ?, room_no = ?, capacity = ?, facility_desc = ?, status = ?, remark = ? where id = ?", request.buildingId(), request.roomNo(), request.capacity(), request.facilityDesc(), request.status() == null ? "enabled" : request.status(), request.remark(), id);
    }

    /**
     * 删除宿舍前检查学生、工单、设施台账等关联数据。
     * 宿舍是报修工单和住宿信息的重要外键来源，为避免历史数据断链，
     * 只允许删除完全未被业务引用的宿舍。
     */
    public void deleteRoom(Long id) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        Integer studentCount = jdbcTemplate.queryForObject("select count(*) from student_profile where room_id = ?", Integer.class, id);
        if (studentCount != null && studentCount > 0) {
            throw new BusinessException("该宿舍仍有入住学生，不能删除");
        }
        Integer orderCount = jdbcTemplate.queryForObject("select count(*) from repair_order where room_id = ?", Integer.class, id);
        if (orderCount != null && orderCount > 0) {
            throw new BusinessException("该宿舍存在关联报修工单，不能删除");
        }
        Integer facilityCount = jdbcTemplate.queryForObject("select count(*) from dorm_facility where room_id = ?", Integer.class, id);
        if (facilityCount != null && facilityCount > 0) {
            throw new BusinessException("该宿舍存在设施台账记录，不能删除");
        }
        jdbcTemplate.update("delete from dorm_room where id = ?", id);
        logService.log(SecurityUtils.currentUser().id(), "楼栋宿舍", "删除", "删除宿舍: " + id);
    }

    public List<Map<String, Object>> students() {
        return commonQueryService.list("select sp.id, sp.student_no as studentNo, u.real_name as realName, u.phone, sp.college, sp.major, sp.class_name as className, sp.building_id as buildingId, sp.room_id as roomId, sp.bed_no as bedNo, db.building_name as buildingName, dr.room_no as roomNo from student_profile sp left join user u on sp.user_id = u.id left join dorm_building db on sp.building_id = db.id left join dorm_room dr on sp.room_id = dr.id order by db.id asc, dr.id asc, sp.id asc");
    }

    public void updateStudentRoom(Long id, StudentRoomRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update("update student_profile set building_id = ?, room_id = ?, bed_no = ?, updated_at = ? where id = ?", request.buildingId(), request.roomId(), request.bedNo(), TimeUtils.now(), id);
    }

    public List<Map<String, Object>> announcements() {
        return commonQueryService.list("select a.id, a.title, a.content, a.image_path as imagePath, a.status, a.published_at as publishedAt, a.created_at as createdAt, u.real_name as publisherName from announcement a left join user u on a.publisher_id = u.id order by a.id desc");
    }

    public void createAnnouncement(AnnouncementRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        JwtUser user = SecurityUtils.currentUser();
        String now = TimeUtils.now();
        jdbcTemplate.update("insert into announcement(title, content, image_path, publisher_id, status, published_at, created_at) values (?, ?, ?, ?, ?, ?, ?)", request.title(), request.content(), request.imagePath(), user.id(), request.status() == null ? "published" : request.status(), now, now);
        logService.log(user.id(), "公告管理", "新增", "发布公告: " + request.title());
    }

    public void updateAnnouncement(Long id, AnnouncementRequest request) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update("update announcement set title = ?, content = ?, image_path = ?, status = ?, published_at = ? where id = ?", request.title(), request.content(), request.imagePath(), request.status() == null ? "published" : request.status(), TimeUtils.now(), id);
    }

    public void deleteAnnouncement(Long id) {
        SecurityUtils.requireRole("dorm_admin", "admin");
        jdbcTemplate.update("delete from announcement where id = ?", id);
    }

    private String baseOrderSql() {
        return "select ro.id, ro.order_no as orderNo, ro.title, ro.description, ro.status, ro.expect_time as expectTime, ro.reject_reason as rejectReason, ro.submitted_at as submittedAt, ro.assigned_at as assignedAt, ro.completed_at as completedAt, rt.type_name as repairTypeName, db.building_name as buildingName, dr.room_no as roomNo, su.real_name as studentName, ru.real_name as repairerName, df.id as facilityId, df.facility_name as facilityName, df.facility_type as facilityType from repair_order ro left join repair_type rt on ro.repair_type_id = rt.id left join dorm_building db on ro.building_id = db.id left join dorm_room dr on ro.room_id = dr.id left join dorm_facility df on ro.facility_id = df.id left join user su on ro.student_id = su.id left join user ru on ro.assigned_repairer_id = ru.id";
    }
}
