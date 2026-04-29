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
        return commonQueryService.list("select u.id, u.username, u.real_name as realName, u.phone, u.avatar, u.role, u.work_type_code as workTypeCode, u.password_question as passwordQuestion, u.password_answer as passwordAnswer, (select group_concat(d.dict_name, '、') from sys_dict d where d.dict_type = 'repair_work_type' and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || d.dict_code || ',') > 0) as workTypeName, u.status, u.created_at as createdAt from user u order by u.id asc");
    }

    public void createUser(UserCreateRequest request) {
        SecurityUtils.requireRole("admin");
        String now = TimeUtils.now();
        String workTypeCode = "repairer".equals(request.role()) ? request.workTypeCode() : null;
        String question = selfServiceRole(request.role()) ? cleanRequired(request.passwordQuestion(), "请设置找回密码问题") : null;
        String answer = selfServiceRole(request.role()) ? cleanRequired(request.passwordAnswer(), "请设置找回密码答案") : null;
        jdbcTemplate.update("insert into user(username, password, real_name, phone, role, work_type_code, password_question, password_answer, status, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", request.username(), request.password(), request.realName(), request.phone(), request.role(), workTypeCode, question, answer, "enabled", now, now);
        logService.log(SecurityUtils.currentUser().id(), "用户管理", "新增", "新增用户: " + request.username());
    }

    public void updateUser(Long id, UserUpdateRequest request) {
        SecurityUtils.requireRole("admin");
        String workTypeCode = "repairer".equals(request.role()) ? request.workTypeCode() : null;
        Map<String, Object> oldUser = commonQueryService.one("select password_answer as passwordAnswer from user where id = ?", id);
        String answer = (String) oldUser.get("passwordAnswer");
        if (selfServiceRole(request.role())) {
            cleanRequired(request.passwordQuestion(), "请设置找回密码问题");
            if (isBlank(answer) && isBlank(request.passwordAnswer())) {
                throw new BusinessException("请设置找回密码答案");
            }
            if (!isBlank(request.passwordAnswer())) {
                answer = request.passwordAnswer().trim();
            }
        } else {
            answer = null;
        }
        String question = selfServiceRole(request.role()) ? request.passwordQuestion().trim() : null;
        jdbcTemplate.update("update user set real_name = ?, phone = ?, role = ?, work_type_code = ?, password_question = ?, password_answer = ?, updated_at = ? where id = ?", request.realName(), request.phone(), request.role(), workTypeCode, question, answer, TimeUtils.now(), id);
        logService.log(SecurityUtils.currentUser().id(), "用户管理", "修改", "修改用户: " + id);
    }

    public void deleteUser(Long id) {
        SecurityUtils.requireRole("admin");
        if (SecurityUtils.currentUser().id().equals(id)) {
            throw new BusinessException("不能删除当前登录账号");
        }
        Map<String, Object> user = commonQueryService.one("select id, username, role from user where id = ?", id);

        // 用户可能被多个业务表引用。为避免删除后工单、评价、公告等历史数据丢失，
        // 只允许删除没有业务数据的账号；学生空档案会随账号一并删除。
        if (countByUser("select count(*) from repair_order where student_id = ? or assigned_repairer_id = ?", id, id) > 0) {
            throw new BusinessException("该用户已关联报修工单，不能删除");
        }
        if (countByUser("select count(*) from repair_flow where operator_id = ?", id) > 0) {
            throw new BusinessException("该用户已关联工单流转记录，不能删除");
        }
        if (countByUser("select count(*) from repair_feedback where repairer_id = ?", id) > 0) {
            throw new BusinessException("该用户已关联维修反馈，不能删除");
        }
        if (countByUser("select count(*) from material_usage where repairer_id = ?", id) > 0) {
            throw new BusinessException("该用户已关联耗材使用记录，不能删除");
        }
        if (countByUser("select count(*) from repair_rating where student_id = ?", id) > 0) {
            throw new BusinessException("该用户已关联服务评价，不能删除");
        }
        if (countByUser("select count(*) from announcement where publisher_id = ?", id) > 0) {
            throw new BusinessException("该用户已发布公告，不能删除");
        }
        if (countByUser("select count(*) from repair_resource where publisher_id = ?", id) > 0) {
            throw new BusinessException("该用户已发布知识内容，不能删除");
        }
        if (countByUser("select count(*) from forum_post where student_id = ?", id) > 0) {
            throw new BusinessException("该用户已发布论坛帖子，不能删除");
        }
        if (countByUser("select count(*) from service_message where student_id = ? or replied_by = ?", id, id) > 0) {
            throw new BusinessException("该用户已关联服务留言，不能删除");
        }

        jdbcTemplate.update("delete from student_profile where user_id = ?", id);
        jdbcTemplate.update("update sys_log set user_id = null where user_id = ?", id);
        jdbcTemplate.update("delete from user where id = ?", id);
        logService.log(SecurityUtils.currentUser().id(), "用户管理", "删除", "删除用户: " + user.get("username"));
    }

    public void updateUserStatus(Long id, StatusUpdateRequest request) {
        SecurityUtils.requireRole("admin");
        jdbcTemplate.update("update user set status = ?, updated_at = ? where id = ?", request.status(), TimeUtils.now(), id);
    }

    private int countByUser(String sql, Object... args) {
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return count == null ? 0 : count;
    }

    private boolean selfServiceRole(String role) {
        return "student".equals(role) || "dorm_admin".equals(role) || "repairer".equals(role);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String cleanRequired(String value, String message) {
        if (isBlank(value)) {
            throw new BusinessException(message);
        }
        return value.trim();
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
        SecurityUtils.requireRole("admin", "dorm_admin");
        return commonQueryService.list(
                "select sm.id, sm.title, sm.content, sm.image_path as imagePath, sm.status, sm.reply_content as replyContent, sm.replied_at as repliedAt, sm.created_at as createdAt, su.real_name as studentName, su.phone as studentPhone, au.real_name as repliedByName " +
                        "from service_message sm left join user su on sm.student_id = su.id left join user au on sm.replied_by = au.id order by sm.id desc"
        );
    }

    public void replyServiceMessage(Long id, ServiceMessageReplyRequest request) {
        SecurityUtils.requireRole("admin", "dorm_admin");
        String now = TimeUtils.now();
        jdbcTemplate.update("update service_message set reply_content = ?, status = ?, replied_by = ?, replied_at = ?, updated_at = ? where id = ?",
                request.replyContent(), "replied", SecurityUtils.currentUser().id(), now, now, id);
        logService.log(SecurityUtils.currentUser().id(), "服务留言", "回复", "回复服务留言: " + id);
    }

    /**
     * 管理员论坛管理列表。
     * 学生端只展示 published 状态的帖子，管理员端需要看到全部状态，便于审核、隐藏和删除。
     */
    public List<Map<String, Object>> forumPosts(String status) {
        SecurityUtils.requireRole("admin", "dorm_admin");
        String sql = "select fp.id, fp.title, fp.content, fp.image_path as imagePath, fp.status, fp.created_at as createdAt, fp.updated_at as updatedAt, " +
                "u.real_name as studentName, u.phone as studentPhone " +
                "from forum_post fp left join user u on fp.student_id = u.id where 1 = 1";
        if (status != null && !status.isBlank()) {
            sql += " and fp.status = '" + status + "'";
        }
        sql += " order by fp.id desc";
        return commonQueryService.list(sql);
    }

    /**
     * 更新论坛帖子状态。
     * published 表示公开展示，hidden 表示管理员隐藏；保留数据可以满足后台追溯需要。
     */
    public void updateForumPostStatus(Long id, StatusUpdateRequest request) {
        SecurityUtils.requireRole("admin", "dorm_admin");
        String status = request.status();
        if (!"published".equals(status) && !"hidden".equals(status)) {
            throw new BusinessException("论坛状态只能设置为 published 或 hidden");
        }
        jdbcTemplate.update("update forum_post set status = ?, updated_at = ? where id = ?", status, TimeUtils.now(), id);
        logService.log(SecurityUtils.currentUser().id(), "论坛管理", "状态变更", "修改论坛帖子状态: " + id + " -> " + status);
    }

    /**
     * 删除论坛帖子。
     * 论坛内容属于学生公开交流数据，管理员保留最终删除能力，用于处理违规或无效帖子。
     */
    public void deleteForumPost(Long id) {
        SecurityUtils.requireRole("admin", "dorm_admin");
        jdbcTemplate.update("delete from forum_post where id = ?", id);
        logService.log(SecurityUtils.currentUser().id(), "论坛管理", "删除", "删除论坛帖子: " + id);
    }

    /**
     * 管理员评价管理列表。
     * 关联工单、学生、维修员和报修类型，让后台可以按服务过程查看评价来源。
     */
    public List<Map<String, Object>> ratings(Integer score) {
        SecurityUtils.requireRole("admin", "dorm_admin");
        String sql = "select rr.id, rr.repair_order_id as repairOrderId, rr.score, rr.content, rr.created_at as createdAt, " +
                "ro.order_no as orderNo, ro.title as orderTitle, rt.type_name as repairTypeName, " +
                "su.real_name as studentName, ru.real_name as repairerName " +
                "from repair_rating rr " +
                "left join repair_order ro on rr.repair_order_id = ro.id " +
                "left join repair_type rt on ro.repair_type_id = rt.id " +
                "left join user su on rr.student_id = su.id " +
                "left join user ru on ro.assigned_repairer_id = ru.id where 1 = 1";
        if (score != null) {
            sql += " and rr.score = " + score;
        }
        sql += " order by rr.id desc";
        return commonQueryService.list(sql);
    }

    /**
     * 删除不合规评价。
     * 这里按业务要求只移除评价展示数据，不回退工单状态，避免影响已完成工单的完成记录和统计口径。
     */
    public void deleteRating(Long id) {
        SecurityUtils.requireRole("admin", "dorm_admin");
        commonQueryService.one("select id from repair_rating where id = ?", id);
        jdbcTemplate.update("delete from repair_rating where id = ?", id);
        logService.log(SecurityUtils.currentUser().id(), "评价管理", "删除", "删除评价记录: " + id);
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
