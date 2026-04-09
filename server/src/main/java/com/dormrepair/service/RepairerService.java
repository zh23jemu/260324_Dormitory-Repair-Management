package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import com.dormrepair.dto.repair.RepairFeedbackRequest;
import com.dormrepair.security.JwtUser;
import com.dormrepair.util.SecurityUtils;
import com.dormrepair.util.TimeUtils;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class RepairerService {

    private final JdbcTemplate jdbcTemplate;
    private final CommonQueryService commonQueryService;
    private final LogService logService;

    public RepairerService(JdbcTemplate jdbcTemplate, CommonQueryService commonQueryService, LogService logService) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonQueryService = commonQueryService;
        this.logService = logService;
    }

    public List<Map<String, Object>> myOrders(String status) {
        SecurityUtils.requireRole("repairer");
        JwtUser user = SecurityUtils.currentUser();
        String sql = "select ro.id, ro.order_no as orderNo, ro.title, ro.description, ro.status, ro.expect_time as expectTime, rt.type_name as repairTypeName, db.building_name as buildingName, dr.room_no as roomNo, su.real_name as studentName from repair_order ro left join repair_type rt on ro.repair_type_id = rt.id left join dorm_building db on ro.building_id = db.id left join dorm_room dr on ro.room_id = dr.id left join user su on ro.student_id = su.id where ro.assigned_repairer_id = ?";
        if (status != null && !status.isBlank()) {
            sql += " and ro.status = '" + status + "'";
        }
        sql += " order by ro.id desc";
        return commonQueryService.list(sql, user.id());
    }

    public Map<String, Object> detail(Long id) {
        SecurityUtils.requireRole("repairer");
        JwtUser user = SecurityUtils.currentUser();
        Map<String, Object> detail = commonQueryService.one("select ro.id, ro.order_no as orderNo, ro.title, ro.description, ro.status, ro.expect_time as expectTime, rt.type_name as repairTypeName, db.building_name as buildingName, dr.room_no as roomNo, su.real_name as studentName, rf.result_desc as resultDesc, rf.materials_used as materialsUsed from repair_order ro left join repair_type rt on ro.repair_type_id = rt.id left join dorm_building db on ro.building_id = db.id left join dorm_room dr on ro.room_id = dr.id left join user su on ro.student_id = su.id left join repair_feedback rf on ro.id = rf.repair_order_id where ro.id = ? and ro.assigned_repairer_id = ?", id, user.id());
        detail.put("images", commonQueryService.list("select id, image_type, file_path, created_at from repair_order_image where repair_order_id = ? order by id asc", id));
        List<Map<String, Object>> ratings = commonQueryService.list("select score, content, created_at from repair_rating where repair_order_id = ?", id);
        detail.put("rating", ratings.isEmpty() ? null : ratings.get(0));
        return detail;
    }

    public void accept(Long id) {
        SecurityUtils.requireRole("repairer");
        JwtUser user = SecurityUtils.currentUser();
        String now = TimeUtils.now();
        jdbcTemplate.update("update repair_order set status = ?, updated_at = ? where id = ? and assigned_repairer_id = ?", "processing", now, id, user.id());
        jdbcTemplate.update("insert into repair_flow(repair_order_id, operator_id, from_status, to_status, remark, created_at) values (?, ?, ?, ?, ?, ?)", id, user.id(), "pending_accept", "processing", "维修人员接单", now);
        logService.log(user.id(), "维修处理", "接单", "接单工单: " + id);
    }

    public void feedback(Long id, RepairFeedbackRequest request) {
        SecurityUtils.requireRole("repairer");
        JwtUser user = SecurityUtils.currentUser();
        String now = TimeUtils.now();
        Integer count = jdbcTemplate.queryForObject("select count(*) from repair_feedback where repair_order_id = ?", Integer.class, id);
        if (count != null && count > 0) {
            jdbcTemplate.update("update repair_feedback set result_desc = ?, materials_used = ?, finish_time = ?, created_at = ? where repair_order_id = ?", request.resultDesc(), request.materialsUsed(), request.finishTime(), now, id);
        } else {
            jdbcTemplate.update("insert into repair_feedback(repair_order_id, repairer_id, result_desc, materials_used, finish_time, created_at) values (?, ?, ?, ?, ?, ?)", id, user.id(), request.resultDesc(), request.materialsUsed(), request.finishTime(), now);
        }
        if (request.imagePaths() != null) {
            for (String imagePath : request.imagePaths()) {
                jdbcTemplate.update("insert into repair_order_image(repair_order_id, image_type, file_path, created_at) values (?, ?, ?, ?)", id, "result", imagePath, now);
            }
        }
        jdbcTemplate.update("update repair_order set status = ?, completed_at = ?, updated_at = ? where id = ?", "pending_rating", request.finishTime(), now, id);
        jdbcTemplate.update("insert into repair_flow(repair_order_id, operator_id, from_status, to_status, remark, created_at) values (?, ?, ?, ?, ?, ?)", id, user.id(), "processing", "pending_rating", "维修完成，等待评价", now);
        logService.log(user.id(), "维修处理", "完成", "提交维修结果: " + id);
    }

    public Map<String, Object> statistics(String dateFrom, String dateTo) {
        SecurityUtils.requireRole("repairer");
        if (dateFrom != null && !dateFrom.isBlank() && dateTo != null && !dateTo.isBlank() && dateFrom.compareTo(dateTo) > 0) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }
        JwtUser user = SecurityUtils.currentUser();
        String rangeClause = "";
        if (dateFrom != null && !dateFrom.isBlank()) {
            rangeClause += " and coalesce(assigned_at, submitted_at) >= '" + dateFrom + " 00:00:00'";
        }
        if (dateTo != null && !dateTo.isBlank()) {
            rangeClause += " and coalesce(assigned_at, submitted_at) <= '" + dateTo + " 23:59:59'";
        }
        Integer total = jdbcTemplate.queryForObject("select count(*) from repair_order where assigned_repairer_id = ?" + rangeClause, Integer.class, user.id());
        Integer completed = jdbcTemplate.queryForObject("select count(*) from repair_order where assigned_repairer_id = ? and status in ('pending_rating', 'completed')" + rangeClause, Integer.class, user.id());
        Integer pending = jdbcTemplate.queryForObject("select count(*) from repair_order where assigned_repairer_id = ? and status = 'pending_accept'" + rangeClause, Integer.class, user.id());
        Integer processing = jdbcTemplate.queryForObject("select count(*) from repair_order where assigned_repairer_id = ? and status = 'processing'" + rangeClause, Integer.class, user.id());
        Double avgHandleHours = jdbcTemplate.queryForObject(
                "select avg((julianday(completed_at) - julianday(assigned_at)) * 24.0) from repair_order where assigned_repairer_id = ? and assigned_at is not null and completed_at is not null and status in ('pending_rating', 'completed')" + rangeClause,
                Double.class,
                user.id()
        );
        double completionRate = total == null || total == 0 ? 0 : (completed == null ? 0 : completed * 100.0 / total);
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("totalCount", total == null ? 0 : total);
        map.put("completedCount", completed == null ? 0 : completed);
        map.put("pendingCount", pending == null ? 0 : pending);
        map.put("processingCount", processing == null ? 0 : processing);
        map.put("completionRate", completionRate);
        map.put("avgHandleHours", avgHandleHours == null ? 0 : Math.round(avgHandleHours * 100.0) / 100.0);
        return map;
    }
}
