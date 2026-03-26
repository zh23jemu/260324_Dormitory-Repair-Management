package com.dormrepair.service;

import com.dormrepair.dto.admin.StatusUpdateRequest;
import com.dormrepair.dto.admin.UserCreateRequest;
import com.dormrepair.dto.admin.UserUpdateRequest;
import com.dormrepair.util.SecurityUtils;
import com.dormrepair.util.TimeUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final JdbcTemplate jdbcTemplate;
    private final CommonQueryService commonQueryService;
    private final LogService logService;

    public AdminService(JdbcTemplate jdbcTemplate, CommonQueryService commonQueryService, LogService logService) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonQueryService = commonQueryService;
        this.logService = logService;
    }

    public List<Map<String, Object>> users() {
        SecurityUtils.requireRole("admin");
        return commonQueryService.list("select id, username, real_name as realName, phone, role, status, created_at as createdAt from user order by id asc");
    }

    public void createUser(UserCreateRequest request) {
        SecurityUtils.requireRole("admin");
        String now = TimeUtils.now();
        jdbcTemplate.update("insert into user(username, password, real_name, phone, role, status, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?)", request.username(), request.password(), request.realName(), request.phone(), request.role(), "enabled", now, now);
        logService.log(SecurityUtils.currentUser().id(), "用户管理", "新增", "新增用户: " + request.username());
    }

    public void updateUser(Long id, UserUpdateRequest request) {
        SecurityUtils.requireRole("admin");
        jdbcTemplate.update("update user set real_name = ?, phone = ?, role = ?, updated_at = ? where id = ?", request.realName(), request.phone(), request.role(), TimeUtils.now(), id);
    }

    public void updateUserStatus(Long id, StatusUpdateRequest request) {
        SecurityUtils.requireRole("admin");
        jdbcTemplate.update("update user set status = ?, updated_at = ? where id = ?", request.status(), TimeUtils.now(), id);
    }

    public Map<String, Object> overview() {
        SecurityUtils.requireRole("admin");
        Map<String, Object> map = new HashMap<>();
        map.put("totalRepairCount", jdbcTemplate.queryForObject("select count(*) from repair_order", Integer.class));
        map.put("pendingReviewCount", jdbcTemplate.queryForObject("select count(*) from repair_order where status = 'pending_review'", Integer.class));
        map.put("processingCount", jdbcTemplate.queryForObject("select count(*) from repair_order where status = 'processing'", Integer.class));
        map.put("completedCount", jdbcTemplate.queryForObject("select count(*) from repair_order where status = 'completed'", Integer.class));
        Double avg = jdbcTemplate.queryForObject("select avg(score) from repair_rating", Double.class);
        map.put("avgScore", avg == null ? 0 : avg);
        return map;
    }

    public List<Map<String, Object>> repairTypeStats() {
        SecurityUtils.requireRole("admin");
        return commonQueryService.list("select rt.type_name as name, count(ro.id) as value from repair_type rt left join repair_order ro on rt.id = ro.repair_type_id group by rt.id, rt.type_name order by rt.sort_no asc");
    }

    public List<Map<String, Object>> buildingHeatStats() {
        SecurityUtils.requireRole("admin");
        return commonQueryService.list("select db.building_name as name, count(ro.id) as value from dorm_building db left join repair_order ro on db.id = ro.building_id group by db.id, db.building_name order by db.id asc");
    }

    public List<Map<String, Object>> ratingStats() {
        SecurityUtils.requireRole("admin");
        return commonQueryService.list("select score as name, count(*) as value from repair_rating group by score order by score asc");
    }

    public List<Map<String, Object>> logs() {
        SecurityUtils.requireRole("admin");
        return commonQueryService.list("select sl.id, sl.module_name as moduleName, sl.operation_type as operationType, sl.operation_desc as operationDesc, sl.ip, sl.created_at as createdAt, u.real_name as userName from sys_log sl left join user u on sl.user_id = u.id order by sl.id desc");
    }

    public List<Map<String, Object>> dicts() {
        SecurityUtils.requireRole("admin");
        return commonQueryService.list("select id, dict_type as dictType, dict_code as dictCode, dict_name as dictName, sort_no as sortNo, status from sys_dict order by id asc");
    }
}
