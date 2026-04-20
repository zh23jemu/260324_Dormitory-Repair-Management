package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import com.dormrepair.dto.repair.RepairFeedbackRequest;
import com.dormrepair.dto.repair.RepairerProfileRequest;
import com.dormrepair.security.JwtUser;
import com.dormrepair.util.SecurityUtils;
import com.dormrepair.util.TimeUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Map<String, Object> myProfile() {
        SecurityUtils.requireRole("repairer");
        JwtUser user = SecurityUtils.currentUser();
        Map<String, Object> profile = commonQueryService.one(
                "select u.id, u.username, u.real_name as realName, u.phone, u.avatar, u.work_type_code as workTypeCode, " +
                        "(select group_concat(d.dict_name, '、') from sys_dict d where d.dict_type = 'repair_work_type' and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || d.dict_code || ',') > 0) as workTypeName " +
                        "from user u where u.id = ? and u.role = 'repairer'",
                user.id()
        );
        profile.put("workTypeCodes", splitCodes((String) profile.get("workTypeCode")));
        profile.put("statistics", statistics(null, null));
        return profile;
    }

    public void updateProfile(RepairerProfileRequest request) {
        SecurityUtils.requireRole("repairer");
        JwtUser user = SecurityUtils.currentUser();
        Map<String, Object> oldProfile = commonQueryService.one("select work_type_code as workTypeCode from user where id = ? and role = 'repairer'", user.id());
        List<String> oldCodes = splitCodes((String) oldProfile.get("workTypeCode"));
        List<String> newCodes = request.workTypeCodes() == null ? List.of() : request.workTypeCodes().stream().map(String::trim).filter((code) -> !code.isBlank()).distinct().toList();
        for (String oldCode : oldCodes) {
            if (!newCodes.contains(oldCode)) {
                /*
                 * 维修员可维护自己的多维修类型，但如果该类型还关联着本人已接单且未完成的工单，
                 * 立即删除会导致工单与工种能力不匹配，因此这里直接阻止保存并提示用户先完成工单。
                 */
                Integer unfinished = jdbcTemplate.queryForObject(
                        "select count(*) from repair_order where assigned_repairer_id = ? and status in ('processing', 'pending_rating') and repair_type_id in (select id from repair_type where (type_name like ? or type_name like ? or type_name like ?))",
                        Integer.class,
                        user.id(),
                        "%" + workTypeKeyword(oldCode, 0) + "%",
                        "%" + workTypeKeyword(oldCode, 1) + "%",
                        "%" + workTypeKeyword(oldCode, 2) + "%"
                );
                if (unfinished != null && unfinished > 0) {
                    throw new BusinessException("该维修类型存在已接单未完成工单，暂不能删除");
                }
            }
        }
        String workTypeCode = normalizeCodes(request.workTypeCodes());
        jdbcTemplate.update("update user set phone = ?, avatar = ?, work_type_code = ?, updated_at = ? where id = ?",
                request.phone(), request.avatar(), workTypeCode, TimeUtils.now(), user.id());
        logService.log(user.id(), "维修员个人信息", "修改", "更新维修员个人信息");
    }

    public List<Map<String, Object>> repairers() {
        SecurityUtils.requireRole("repairer");
        return commonQueryService.list(
                "select u.id, u.real_name as realName, u.phone, u.avatar, u.work_type_code as workTypeCode, " +
                        "(select group_concat(d.dict_name, '、') from sys_dict d where d.dict_type = 'repair_work_type' and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || d.dict_code || ',') > 0) as workTypeName, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id and ro.status in ('processing', 'pending_rating', 'completed')) as acceptedCount, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id and ro.status in ('pending_rating', 'completed')) as completedCount, " +
                        "(select round(avg(rr.score), 2) from repair_rating rr left join repair_order ro on rr.repair_order_id = ro.id where ro.assigned_repairer_id = u.id) as avgScore " +
                        "from user u where u.role = 'repairer' and u.status = 'enabled' order by u.id asc"
        );
    }

    public Map<String, Object> repairerDetail(Long id) {
        SecurityUtils.requireRole("repairer");
        Map<String, Object> detail = commonQueryService.one(
                "select u.id, u.username, u.real_name as realName, u.phone, u.avatar, u.work_type_code as workTypeCode, " +
                        "(select group_concat(d.dict_name, '、') from sys_dict d where d.dict_type = 'repair_work_type' and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || d.dict_code || ',') > 0) as workTypeName, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id and ro.status in ('processing', 'pending_rating', 'completed')) as acceptedCount, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id and ro.status in ('pending_rating', 'completed')) as completedCount, " +
                        "(select round(avg(rr.score), 2) from repair_rating rr left join repair_order ro on rr.repair_order_id = ro.id where ro.assigned_repairer_id = u.id) as avgScore " +
                        "from user u where u.role = 'repairer' and u.id = ?",
                id
        );
        int accepted = detail.get("acceptedCount") == null ? 0 : ((Number) detail.get("acceptedCount")).intValue();
        int completed = detail.get("completedCount") == null ? 0 : ((Number) detail.get("completedCount")).intValue();
        detail.put("completionRate", accepted == 0 ? 0 : Math.round(completed * 10000.0 / accepted) / 100.0);
        detail.put("ratings", commonQueryService.list(
                "select ro.id, ro.order_no as orderNo, ro.title, rt.type_name as repairTypeName, rr.score, rr.content, rr.created_at as createdAt, su.real_name as studentName " +
                        "from repair_order ro left join repair_type rt on ro.repair_type_id = rt.id left join repair_rating rr on ro.id = rr.repair_order_id left join user su on rr.student_id = su.id " +
                        "where ro.assigned_repairer_id = ? and ro.status = 'completed' and rr.id is not null order by rr.id desc",
                id
        ));
        return detail;
    }

    public List<Map<String, Object>> workTypes() {
        SecurityUtils.requireRole("repairer");
        return commonQueryService.list("select id, dict_code as dictCode, dict_name as dictName, sort_no as sortNo from sys_dict where dict_type = 'repair_work_type' and status = 'enabled' order by sort_no asc, id asc");
    }

    public List<Map<String, Object>> materials() {
        SecurityUtils.requireRole("repairer");
        return commonQueryService.list("select id, material_name as materialName, material_type as materialType, unit, stock_qty as stockQty, warning_qty as warningQty, remark from repair_material order by id asc");
    }

    public Map<String, Object> detail(Long id) {
        SecurityUtils.requireRole("repairer");
        JwtUser user = SecurityUtils.currentUser();
        Map<String, Object> detail = commonQueryService.one("select ro.id, ro.order_no as orderNo, ro.title, ro.description, ro.status, ro.expect_time as expectTime, rt.type_name as repairTypeName, db.building_name as buildingName, dr.room_no as roomNo, su.real_name as studentName, rf.result_desc as resultDesc, rf.materials_used as materialsUsed, df.facility_name as facilityName, df.facility_type as facilityType from repair_order ro left join repair_type rt on ro.repair_type_id = rt.id left join dorm_building db on ro.building_id = db.id left join dorm_room dr on ro.room_id = dr.id left join dorm_facility df on ro.facility_id = df.id left join user su on ro.student_id = su.id left join repair_feedback rf on ro.id = rf.repair_order_id where ro.id = ? and ro.assigned_repairer_id = ?", id, user.id());
        detail.put("images", commonQueryService.list("select id, image_type, file_path, created_at from repair_order_image where repair_order_id = ? order by id asc", id));
        detail.put("flows", commonQueryService.list("select rf.id, rf.from_status, rf.to_status, rf.remark, rf.created_at, u.real_name as operator_name from repair_flow rf left join user u on rf.operator_id = u.id where rf.repair_order_id = ? order by rf.id asc", id));
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

    @Transactional
    public void feedback(Long id, RepairFeedbackRequest request) {
        SecurityUtils.requireRole("repairer");
        JwtUser user = SecurityUtils.currentUser();
        String now = TimeUtils.now();
        Map<String, Object> order = commonQueryService.one("select id, status from repair_order where id = ? and assigned_repairer_id = ?", id, user.id());
        if (!"processing".equals(order.get("status"))) {
            throw new BusinessException("只有处理中工单可以提交维修结果，避免重复扣减耗材");
        }
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
        deductMaterials(id, user.id(), request.materialUsages(), now);
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
        Integer total = jdbcTemplate.queryForObject("select count(*) from repair_order where assigned_repairer_id = ? and status in ('processing', 'pending_rating', 'completed')" + rangeClause, Integer.class, user.id());
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

    private void deductMaterials(Long orderId, Long repairerId, List<RepairFeedbackRequest.MaterialUsageItem> materialUsages, String now) {
        if (materialUsages == null || materialUsages.isEmpty()) {
            return;
        }
        /*
         * 同一工单可以上报多种耗材，也可能因为前端连续添加出现同一耗材多行。
         * 这里先按耗材 ID 汇总，再统一校验库存，避免“第一行扣减成功、第二行库存不足”的半完成状态。
         */
        Map<Long, Double> usageMap = new LinkedHashMap<>();
        for (RepairFeedbackRequest.MaterialUsageItem item : materialUsages) {
            if (item.materialId() == null || item.quantity() == null) {
                throw new BusinessException("请完整选择耗材并填写使用数量");
            }
            if (item.quantity() <= 0) {
                throw new BusinessException("耗材使用数量必须大于 0");
            }
            usageMap.merge(item.materialId(), item.quantity(), Double::sum);
        }
        for (Map.Entry<Long, Double> entry : usageMap.entrySet()) {
            Map<String, Object> material = commonQueryService.one("select id, material_name as materialName, stock_qty as stockQty from repair_material where id = ?", entry.getKey());
            double stockQty = material.get("stockQty") == null ? 0 : ((Number) material.get("stockQty")).doubleValue();
            if (stockQty < entry.getValue()) {
                throw new BusinessException("耗材库存不足：" + material.get("materialName"));
            }
        }
        for (Map.Entry<Long, Double> entry : usageMap.entrySet()) {
            jdbcTemplate.update("update repair_material set stock_qty = stock_qty - ?, updated_at = ? where id = ?", entry.getValue(), now, entry.getKey());
            jdbcTemplate.update("insert into material_usage(repair_order_id, repairer_id, material_id, quantity, created_at) values (?, ?, ?, ?, ?)", orderId, repairerId, entry.getKey(), entry.getValue(), now);
        }
    }

    private List<String> splitCodes(String codes) {
        if (codes == null || codes.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(codes.split(",")).map(String::trim).filter((code) -> !code.isBlank()).toList();
    }

    private String normalizeCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return null;
        }
        return codes.stream().map(String::trim).filter((code) -> !code.isBlank()).distinct().collect(Collectors.joining(","));
    }

    /**
     * 将维修工种编码映射为报修类型名称中的关键词，用于判断“删除工种时是否仍有关联未完成工单”。
     * 当前项目的报修类型仍是业务主数据表，和工种字典没有外键关系，因此这里沿用分配推荐时的中文关键词口径。
     */
    private String workTypeKeyword(String code, int index) {
        if ("electrician".equals(code)) {
            return index == 0 ? "水" : index == 1 ? "电" : "灯";
        }
        if ("carpenter".equals(code)) {
            return index == 0 ? "家具" : index == 1 ? "门" : "床";
        }
        if ("network".equals(code)) {
            return index == 0 ? "网" : index == 1 ? "宽带" : "路由";
        }
        return code;
    }
}
