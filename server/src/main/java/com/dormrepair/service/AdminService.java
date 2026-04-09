package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import com.dormrepair.dto.admin.DictSaveRequest;
import com.dormrepair.dto.admin.RepairTypeSaveRequest;
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
        return commonQueryService.list("select u.id, u.username, u.real_name as realName, u.phone, u.role, u.work_type_code as workTypeCode, d.dict_name as workTypeName, u.status, u.created_at as createdAt from user u left join sys_dict d on u.work_type_code = d.dict_code and d.dict_type = 'repair_work_type' order by u.id asc");
    }

    public void createUser(UserCreateRequest request) {
        SecurityUtils.requireRole("admin");
        String now = TimeUtils.now();
        String workTypeCode = "repairer".equals(request.role()) ? request.workTypeCode() : null;
        jdbcTemplate.update("insert into user(username, password, real_name, phone, role, work_type_code, status, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?)", request.username(), request.password(), request.realName(), request.phone(), request.role(), workTypeCode, "enabled", now, now);
        logService.log(SecurityUtils.currentUser().id(), "用户管理", "新增", "新增用户: " + request.username());
    }

    public void updateUser(Long id, UserUpdateRequest request) {
        SecurityUtils.requireRole("admin");
        String workTypeCode = "repairer".equals(request.role()) ? request.workTypeCode() : null;
        jdbcTemplate.update("update user set real_name = ?, phone = ?, role = ?, work_type_code = ?, updated_at = ? where id = ?", request.realName(), request.phone(), request.role(), workTypeCode, TimeUtils.now(), id);
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
        map.put("completedCount", jdbcTemplate.queryForObject("select count(*) from repair_order where status in ('pending_rating', 'completed')", Integer.class));
        map.put("pendingRatingCount", jdbcTemplate.queryForObject("select count(*) from repair_order where status = 'pending_rating'", Integer.class));
        map.put("ratedCount", jdbcTemplate.queryForObject("select count(*) from repair_rating", Integer.class));
        Double avg = jdbcTemplate.queryForObject("select avg(score) from repair_rating", Double.class);
        map.put("avgScore", avg == null ? 0 : avg);
        Integer total = (Integer) map.get("totalRepairCount");
        Integer completed = (Integer) map.get("completedCount");
        Integer ratedCount = (Integer) map.get("ratedCount");
        Integer satisfiedCount = jdbcTemplate.queryForObject("select count(*) from repair_rating where score >= 4", Integer.class);
        map.put("completionRate", total == null || total == 0 ? 0 : completed * 100.0 / total);
        map.put("satisfactionRate", ratedCount == null || ratedCount == 0 ? 0 : (satisfiedCount == null ? 0 : satisfiedCount * 100.0 / ratedCount));
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

    public void createDict(DictSaveRequest request) {
        SecurityUtils.requireRole("admin");
        Integer count = jdbcTemplate.queryForObject("select count(*) from sys_dict where dict_type = ? and dict_code = ?", Integer.class, request.dictType(), request.dictCode());
        if (count != null && count > 0) {
            throw new BusinessException("同类型下字典编码已存在");
        }
        jdbcTemplate.update("insert into sys_dict(dict_type, dict_code, dict_name, sort_no, status) values (?, ?, ?, ?, ?)", request.dictType(), request.dictCode(), request.dictName(), request.sortNo(), request.status());
        logService.log(SecurityUtils.currentUser().id(), "基础配置", "新增", "新增字典项: " + request.dictType() + "/" + request.dictCode());
    }

    public void updateDict(Long id, DictSaveRequest request) {
        SecurityUtils.requireRole("admin");
        Integer count = jdbcTemplate.queryForObject("select count(*) from sys_dict where dict_type = ? and dict_code = ? and id <> ?", Integer.class, request.dictType(), request.dictCode(), id);
        if (count != null && count > 0) {
            throw new BusinessException("同类型下字典编码已存在");
        }
        jdbcTemplate.update("update sys_dict set dict_type = ?, dict_code = ?, dict_name = ?, sort_no = ?, status = ? where id = ?", request.dictType(), request.dictCode(), request.dictName(), request.sortNo(), request.status(), id);
        logService.log(SecurityUtils.currentUser().id(), "基础配置", "修改", "修改字典项: " + id);
    }

    public void deleteDict(Long id) {
        SecurityUtils.requireRole("admin");
        Map<String, Object> dict = commonQueryService.one("select id, dict_type as dictType, dict_code as dictCode from sys_dict where id = ?", id);
        if ("repair_work_type".equals(dict.get("dictType"))) {
            Integer count = jdbcTemplate.queryForObject("select count(*) from user where work_type_code = ?", Integer.class, dict.get("dictCode"));
            if (count != null && count > 0) {
                throw new BusinessException("该维修工种已被用户使用，不能删除");
            }
        }
        jdbcTemplate.update("delete from sys_dict where id = ?", id);
        logService.log(SecurityUtils.currentUser().id(), "基础配置", "删除", "删除字典项: " + id);
    }

    public List<Map<String, Object>> repairTypes() {
        SecurityUtils.requireRole("admin");
        return commonQueryService.list("select id, type_name as typeName, sort_no as sortNo, status from repair_type order by sort_no asc, id asc");
    }

    public void createRepairType(RepairTypeSaveRequest request) {
        SecurityUtils.requireRole("admin");
        Integer count = jdbcTemplate.queryForObject("select count(*) from repair_type where type_name = ?", Integer.class, request.typeName());
        if (count != null && count > 0) {
            throw new BusinessException("报修类型已存在");
        }
        jdbcTemplate.update("insert into repair_type(type_name, sort_no, status) values (?, ?, ?)", request.typeName(), request.sortNo(), request.status());
        logService.log(SecurityUtils.currentUser().id(), "基础配置", "新增", "新增报修类型: " + request.typeName());
    }

    public void updateRepairType(Long id, RepairTypeSaveRequest request) {
        SecurityUtils.requireRole("admin");
        Integer count = jdbcTemplate.queryForObject("select count(*) from repair_type where type_name = ? and id <> ?", Integer.class, request.typeName(), id);
        if (count != null && count > 0) {
            throw new BusinessException("报修类型已存在");
        }
        jdbcTemplate.update("update repair_type set type_name = ?, sort_no = ?, status = ? where id = ?", request.typeName(), request.sortNo(), request.status(), id);
        logService.log(SecurityUtils.currentUser().id(), "基础配置", "修改", "修改报修类型: " + id);
    }

    public void deleteRepairType(Long id) {
        SecurityUtils.requireRole("admin");
        Integer count = jdbcTemplate.queryForObject("select count(*) from repair_order where repair_type_id = ?", Integer.class, id);
        if (count != null && count > 0) {
            throw new BusinessException("该报修类型已被工单使用，不能删除");
        }
        jdbcTemplate.update("delete from repair_type where id = ?", id);
        logService.log(SecurityUtils.currentUser().id(), "基础配置", "删除", "删除报修类型: " + id);
    }
}
