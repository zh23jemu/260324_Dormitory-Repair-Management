package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import com.dormrepair.dto.student.ForumCommentRequest;
import com.dormrepair.security.JwtUser;
import com.dormrepair.util.SecurityUtils;
import com.dormrepair.util.TimeUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PortalService {

    private final JdbcTemplate jdbcTemplate;
    private final CommonQueryService commonQueryService;
    private final LogService logService;

    public PortalService(JdbcTemplate jdbcTemplate, CommonQueryService commonQueryService, LogService logService) {
        this.jdbcTemplate = jdbcTemplate;
        this.commonQueryService = commonQueryService;
        this.logService = logService;
    }

    /**
     * 公共门户首页数据。
     * 用于统一门户首页展示公告、全部报修记录、服务统计、维修排行和论坛动态，不要求登录。
     */
    public Map<String, Object> home() {
        Map<String, Object> map = new HashMap<>();
        map.put("announcements", announcements());
        map.put("orders", repairOrders());
        map.put("statistics", statistics());
        map.put("repairerRanking", repairerRanking());
        map.put("forumPosts", forumPosts());
        return map;
    }

    public List<Map<String, Object>> announcements() {
        return commonQueryService.list(
                "select a.id, a.title, a.content, a.image_path as imagePath, a.published_at as publishedAt, a.created_at as createdAt, " +
                        "u.username as publisherUsername, u.real_name as publisherName, u.avatar as publisherAvatar " +
                        "from announcement a left join user u on a.publisher_id = u.id " +
                        "where a.status = 'published' order by coalesce(a.published_at, a.created_at) desc, a.id desc"
        );
    }

    public List<Map<String, Object>> repairOrders() {
        return commonQueryService.list(
                "select ro.id, ro.order_no as orderNo, ro.title, ro.description, ro.status, ro.submitted_at as submittedAt, ro.completed_at as completedAt, " +
                        "rt.type_name as repairTypeName, db.building_name as buildingName, dr.room_no as roomNo, " +
                        "su.username as studentUsername, su.real_name as studentName, su.avatar as studentAvatar, ru.real_name as repairerName " +
                        "from repair_order ro " +
                        "left join repair_type rt on ro.repair_type_id = rt.id " +
                        "left join dorm_building db on ro.building_id = db.id " +
                        "left join dorm_room dr on ro.room_id = dr.id " +
                        "left join user su on ro.student_id = su.id " +
                        "left join user ru on ro.assigned_repairer_id = ru.id " +
                        "order by ro.id desc limit 30"
        );
    }

    public List<Map<String, Object>> forumPosts() {
        List<Map<String, Object>> posts = commonQueryService.list(
                "select fp.id, fp.title, fp.content, fp.image_path as imagePath, fp.created_at as createdAt, fp.updated_at as updatedAt, " +
                        "u.username as username, u.real_name as studentName, u.avatar as avatar, " +
                        "0 as commentCount " +
                        "from forum_post fp left join user u on fp.student_id = u.id " +
                        "where fp.status = 'published' order by fp.id desc limit 20"
        );
        for (Map<String, Object> post : posts) {
            post.put("comments", List.of());
        }
        return posts;
    }

    public List<Map<String, Object>> repairers() {
        return commonQueryService.list(
                "select u.id, u.username, u.real_name as realName, u.phone, u.avatar, u.work_type_code as workTypeCode, " +
                        "(select group_concat(d.dict_name, '、') from sys_dict d where d.dict_type = 'repair_work_type' and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || d.dict_code || ',') > 0) as workTypeName, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id) as totalCount, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id and ro.status in ('pending_rating', 'completed')) as completedCount, " +
                        "(select round(avg(rr.score), 2) from repair_rating rr left join repair_order ro on rr.repair_order_id = ro.id where ro.assigned_repairer_id = u.id) as avgScore " +
                        "from user u where u.role = 'repairer' and u.status = 'enabled' order by completedCount desc, totalCount desc, u.id asc"
        );
    }

    public Map<String, Object> repairerDetail(Long id) {
        Map<String, Object> detail = commonQueryService.one(
                "select u.id, u.username, u.real_name as realName, u.phone, u.avatar, u.work_type_code as workTypeCode, " +
                        "(select group_concat(d.dict_name, '、') from sys_dict d where d.dict_type = 'repair_work_type' and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || d.dict_code || ',') > 0) as workTypeName, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id) as totalCount, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id and ro.status in ('pending_rating', 'completed')) as completedCount, " +
                        "(select round(avg(rr.score), 2) from repair_rating rr left join repair_order ro on rr.repair_order_id = ro.id where ro.assigned_repairer_id = u.id) as avgScore " +
                        "from user u where u.role = 'repairer' and u.status = 'enabled' and u.id = ?",
                id
        );
        int total = detail.get("totalCount") == null ? 0 : ((Number) detail.get("totalCount")).intValue();
        int completed = detail.get("completedCount") == null ? 0 : ((Number) detail.get("completedCount")).intValue();
        detail.put("completionRate", total == 0 ? 0 : Math.round(completed * 10000.0 / total) / 100.0);
        detail.put("recentOrders", commonQueryService.list(
                "select ro.id, ro.order_no as orderNo, ro.title, ro.status, ro.completed_at as completedAt, rt.type_name as repairTypeName, " +
                        "rr.score, rr.content as ratingContent, rr.created_at as ratedAt, su.real_name as studentName " +
                        "from repair_order ro " +
                        "left join repair_type rt on ro.repair_type_id = rt.id " +
                        "left join repair_rating rr on ro.id = rr.repair_order_id " +
                        "left join user su on rr.student_id = su.id " +
                        "where ro.assigned_repairer_id = ? and ro.status = 'completed' order by ro.id desc limit 12",
                id
        ));
        return detail;
    }

    public void createForumComment(Long postId, ForumCommentRequest request) {
        JwtUser user = SecurityUtils.currentUser();
        Integer postCount = jdbcTemplate.queryForObject("select count(*) from forum_post where id = ? and status = 'published'", Integer.class, postId);
        if (postCount == null || postCount == 0) {
            throw new BusinessException("帖子不存在或已隐藏");
        }
        String now = TimeUtils.now();
        logService.log(user.id(), "论坛评论", "新增", "评论论坛帖子: " + postId + "，内容：" + request.content());
    }

    private Map<String, Object> statistics() {
        Map<String, Object> map = new HashMap<>();
        Integer total = jdbcTemplate.queryForObject("select count(*) from repair_order", Integer.class);
        Integer completed = jdbcTemplate.queryForObject("select count(*) from repair_order where status in ('pending_rating', 'completed')", Integer.class);
        Integer processing = jdbcTemplate.queryForObject("select count(*) from repair_order where status = 'processing'", Integer.class);
        Integer forumCount = jdbcTemplate.queryForObject("select count(*) from forum_post where status = 'published'", Integer.class);
        Integer announcementCount = jdbcTemplate.queryForObject("select count(*) from announcement where status = 'published'", Integer.class);
        Double avgScore = jdbcTemplate.queryForObject("select avg(score) from repair_rating", Double.class);
        map.put("totalCount", total == null ? 0 : total);
        map.put("completedCount", completed == null ? 0 : completed);
        map.put("processingCount", processing == null ? 0 : processing);
        map.put("forumCount", forumCount == null ? 0 : forumCount);
        map.put("announcementCount", announcementCount == null ? 0 : announcementCount);
        map.put("completionRate", total == null || total == 0 ? 0 : Math.round((completed == null ? 0 : completed) * 10000.0 / total) / 100.0);
        map.put("avgScore", avgScore == null ? 0 : Math.round(avgScore * 100.0) / 100.0);
        return map;
    }

    private List<Map<String, Object>> repairerRanking() {
        return commonQueryService.list(
                "select u.id, u.username, u.real_name as realName, u.avatar, " +
                        "(select group_concat(d.dict_name, '、') from sys_dict d where d.dict_type = 'repair_work_type' and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || d.dict_code || ',') > 0) as workTypeName, " +
                        "count(ro.id) as totalCount, sum(case when ro.status in ('pending_rating', 'completed') then 1 else 0 end) as completedCount, " +
                        "round(avg(rr.score), 2) as avgScore " +
                        "from user u " +
                        "left join repair_order ro on u.id = ro.assigned_repairer_id " +
                        "left join repair_rating rr on ro.id = rr.repair_order_id " +
                        "where u.role = 'repairer' " +
                        "group by u.id, u.username, u.real_name, u.avatar, u.work_type_code " +
                        "order by completedCount desc, totalCount desc, u.id asc limit 8"
        );
    }
}
