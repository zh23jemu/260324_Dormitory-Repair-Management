package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import com.dormrepair.dto.admin.DictSaveRequest;
import com.dormrepair.dto.admin.MaterialRequest;
import com.dormrepair.dto.admin.ResourceRequest;
import com.dormrepair.dto.admin.RepairTypeSaveRequest;
import com.dormrepair.dto.admin.ServiceMessageReplyRequest;
import com.dormrepair.dto.admin.StatusUpdateRequest;
import com.dormrepair.dto.admin.UserCreateRequest;
import com.dormrepair.dto.admin.UserUpdateRequest;
import com.dormrepair.util.SecurityUtils;
import com.dormrepair.util.TimeUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        return commonQueryService.list("select u.id, u.username, u.real_name as realName, u.phone, u.avatar, u.role, u.work_type_code as workTypeCode, (select group_concat(d.dict_name, '、') from sys_dict d where d.dict_type = 'repair_work_type' and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || d.dict_code || ',') > 0) as workTypeName, u.status, u.created_at as createdAt from user u order by u.id asc");
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
        Double avgHandleHours = jdbcTemplate.queryForObject("select avg((julianday(completed_at) - julianday(assigned_at)) * 24.0) from repair_order where assigned_at is not null and completed_at is not null and status in ('pending_rating', 'completed')", Double.class);
        map.put("completionRate", total == null || total == 0 ? 0 : completed * 100.0 / total);
        map.put("satisfactionRate", ratedCount == null || ratedCount == 0 ? 0 : (satisfiedCount == null ? 0 : satisfiedCount * 100.0 / ratedCount));
        map.put("avgHandleHours", avgHandleHours == null ? 0 : Math.round(avgHandleHours * 100.0) / 100.0);
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

    public List<Map<String, Object>> statusStats() {
        SecurityUtils.requireRole("admin");
        return commonQueryService.list("select status as name, count(*) as value from repair_order group by status order by count(*) desc");
    }

    public List<Map<String, Object>> repairerWorkloadStats() {
        SecurityUtils.requireRole("admin");
        return commonQueryService.list(
                "select u.real_name as name, count(ro.id) as value from user u left join repair_order ro on u.id = ro.assigned_repairer_id where u.role = 'repairer' group by u.id, u.real_name order by value desc, u.id asc"
        );
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
            Integer unfinished = jdbcTemplate.queryForObject(
                    "select count(*) from repair_order ro left join user u on ro.assigned_repairer_id = u.id where ro.status in ('processing', 'pending_rating') and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || ? || ',') > 0",
                    Integer.class,
                    dict.get("dictCode")
            );
            if (unfinished != null && unfinished > 0) {
                throw new BusinessException("该维修工种存在已接单未完成工单，不能删除");
            }
            /*
             * 维修工种是多选编码，删除一个工种配置时，如果没有未完成已接单工单，
             * 需要同步从维修员资料中移除该编码，避免后续页面展示出已删除的无效工种。
             */
            String dictCode = String.valueOf(dict.get("dictCode"));
            List<Map<String, Object>> repairers = commonQueryService.list(
                    "select id, work_type_code as workTypeCode from user where role = 'repairer' and instr(',' || coalesce(work_type_code, '') || ',', ',' || ? || ',') > 0",
                    dictCode
            );
            for (Map<String, Object> repairer : repairers) {
                String updatedCodes = removeCode((String) repairer.get("workTypeCode"), dictCode);
                jdbcTemplate.update("update user set work_type_code = ?, updated_at = ? where id = ?", updatedCodes, TimeUtils.now(), repairer.get("id"));
            }
        }
        jdbcTemplate.update("delete from sys_dict where id = ?", id);
        logService.log(SecurityUtils.currentUser().id(), "基础配置", "删除", "删除字典项: " + id);
    }

    /**
     * 从逗号分隔的维修工种编码中移除指定编码。
     * 返回 null 而不是空字符串，便于数据库中明确表示“未设置工种”。
     */
    private String removeCode(String codes, String removedCode) {
        if (codes == null || codes.isBlank()) {
            return null;
        }
        String result = Arrays.stream(codes.split(","))
                .map(String::trim)
                .filter((code) -> !code.isBlank())
                .filter((code) -> !code.equals(removedCode))
                .distinct()
                .collect(Collectors.joining(","));
        return result.isBlank() ? null : result;
    }

    public List<Map<String, Object>> resources() {
        SecurityUtils.requireRole("admin");
        return commonQueryService.list("select rr.id, rr.title, rr.category, rr.summary, rr.content, rr.cover_image as coverImage, rr.sort_no as sortNo, rr.status, rr.created_at as createdAt, u.real_name as publisherName from repair_resource rr left join user u on rr.publisher_id = u.id order by rr.sort_no asc, rr.id desc");
    }

    public void createResource(ResourceRequest request) {
        SecurityUtils.requireRole("admin");
        String now = TimeUtils.now();
        jdbcTemplate.update(
                "insert into repair_resource(title, category, summary, content, cover_image, sort_no, status, publisher_id, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                request.title(), request.category(), request.summary(), request.content(), request.coverImage(), request.sortNo(),
                request.status() == null ? "published" : request.status(), SecurityUtils.currentUser().id(), now, now
        );
        logService.log(SecurityUtils.currentUser().id(), "报修知识", "新增", "新增知识库内容: " + request.title());
    }

    public void updateResource(Long id, ResourceRequest request) {
        SecurityUtils.requireRole("admin");
        jdbcTemplate.update(
                "update repair_resource set title = ?, category = ?, summary = ?, content = ?, cover_image = ?, sort_no = ?, status = ?, updated_at = ? where id = ?",
                request.title(), request.category(), request.summary(), request.content(), request.coverImage(), request.sortNo(),
                request.status() == null ? "published" : request.status(), TimeUtils.now(), id
        );
        logService.log(SecurityUtils.currentUser().id(), "报修知识", "修改", "修改知识库内容: " + id);
    }

    public void deleteResource(Long id) {
        SecurityUtils.requireRole("admin");
        jdbcTemplate.update("delete from repair_resource where id = ?", id);
        logService.log(SecurityUtils.currentUser().id(), "报修知识", "删除", "删除知识库内容: " + id);
    }

    public List<Map<String, Object>> serviceMessages() {
        SecurityUtils.requireRole("admin");
        return commonQueryService.list(
                "select sm.id, sm.title, sm.content, sm.image_path as imagePath, sm.status, sm.reply_content as replyContent, sm.replied_at as repliedAt, sm.created_at as createdAt, su.real_name as studentName, su.phone as studentPhone, au.real_name as repliedByName " +
                        "from service_message sm left join user su on sm.student_id = su.id left join user au on sm.replied_by = au.id order by sm.id desc"
        );
    }

    public void replyServiceMessage(Long id, ServiceMessageReplyRequest request) {
        SecurityUtils.requireRole("admin");
        String now = TimeUtils.now();
        jdbcTemplate.update("update service_message set reply_content = ?, status = ?, replied_by = ?, replied_at = ?, updated_at = ? where id = ?",
                request.replyContent(), "replied", SecurityUtils.currentUser().id(), now, now, id);
        logService.log(SecurityUtils.currentUser().id(), "服务留言", "回复", "回复服务留言: " + id);
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
        Integer sortCount = jdbcTemplate.queryForObject("select count(*) from repair_type where sort_no = ?", Integer.class, request.sortNo());
        if (sortCount != null && sortCount > 0) {
            throw new BusinessException("排序号已存在");
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
        Integer sortCount = jdbcTemplate.queryForObject("select count(*) from repair_type where sort_no = ? and id <> ?", Integer.class, request.sortNo(), id);
        if (sortCount != null && sortCount > 0) {
            throw new BusinessException("排序号已存在");
        }
        jdbcTemplate.update("update repair_type set type_name = ?, sort_no = ?, status = ? where id = ?", request.typeName(), request.sortNo(), request.status(), id);
        logService.log(SecurityUtils.currentUser().id(), "基础配置", "修改", "修改报修类型: " + id);
    }

    public void deleteRepairType(Long id) {
        SecurityUtils.requireRole("admin");
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from repair_order where repair_type_id = ? and status not in ('completed', 'rejected')",
                Integer.class,
                id
        );
        if (count != null && count > 0) {
            throw new BusinessException("该报修类型存在未完成工单，不能删除");
        }
        jdbcTemplate.update("delete from repair_type where id = ?", id);
        logService.log(SecurityUtils.currentUser().id(), "基础配置", "删除", "删除报修类型: " + id);
    }

    public List<Map<String, Object>> materials() {
        SecurityUtils.requireRole("admin");
        return commonQueryService.list("select id, material_name as materialName, material_type as materialType, unit, stock_qty as stockQty, warning_qty as warningQty, remark, created_at as createdAt, updated_at as updatedAt from repair_material order by id asc");
    }

    public void createMaterial(MaterialRequest request) {
        SecurityUtils.requireRole("admin");
        String now = TimeUtils.now();
        jdbcTemplate.update(
                "insert into repair_material(material_name, material_type, unit, stock_qty, warning_qty, remark, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?)",
                request.materialName(), request.materialType(), request.unit(), request.stockQty(), request.warningQty(), request.remark(), now, now
        );
        logService.log(SecurityUtils.currentUser().id(), "耗材管理", "新增", "新增耗材: " + request.materialName());
    }

    public void updateMaterial(Long id, MaterialRequest request) {
        SecurityUtils.requireRole("admin");
        jdbcTemplate.update(
                "update repair_material set material_name = ?, material_type = ?, unit = ?, stock_qty = ?, warning_qty = ?, remark = ?, updated_at = ? where id = ?",
                request.materialName(), request.materialType(), request.unit(), request.stockQty(), request.warningQty(), request.remark(), TimeUtils.now(), id
        );
        logService.log(SecurityUtils.currentUser().id(), "耗材管理", "修改", "修改耗材: " + id);
    }

    public void deleteMaterial(Long id) {
        SecurityUtils.requireRole("admin");
        Integer count = jdbcTemplate.queryForObject("select count(*) from material_usage where material_id = ?", Integer.class, id);
        if (count != null && count > 0) {
            throw new BusinessException("该耗材已有使用记录，不能删除");
        }
        jdbcTemplate.update("delete from repair_material where id = ?", id);
        logService.log(SecurityUtils.currentUser().id(), "耗材管理", "删除", "删除耗材: " + id);
    }
}
