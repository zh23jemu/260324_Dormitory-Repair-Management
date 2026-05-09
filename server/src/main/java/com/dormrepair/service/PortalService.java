package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import com.dormrepair.dto.student.ForumCommentRequest;
import com.dormrepair.security.JwtUser;
import com.dormrepair.util.SecurityUtils;
import com.dormrepair.util.TimeUtils;
import java.util.ArrayList;
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
     * 用于统一门户首页展示公告、全部报修记录、服务统计和维修排行，不再展示公开论坛内容。
     */
    public Map<String, Object> home() {
        Map<String, Object> map = new HashMap<>();
        map.put("announcements", announcements(1, 6).get("records"));
        map.put("orders", repairOrders(1, 8).get("records"));
        map.put("statistics", statistics());
        map.put("repairerRanking", repairerRanking());
        return map;
    }

    public Map<String, Object> announcements(Integer pageNum, Integer pageSize) {
        return commonQueryService.page(
                "select a.id, a.title, a.content, a.image_path as imagePath, a.published_at as publishedAt, a.created_at as createdAt, " +
                        "u.username as publisherUsername, u.real_name as publisherName, u.avatar as publisherAvatar " +
                        "from announcement a left join user u on a.publisher_id = u.id " +
                        "where a.status = 'published' order by coalesce(a.published_at, a.created_at) desc, a.id desc",
                pageNum,
                pageSize
        );
    }

    public Map<String, Object> repairOrders(Integer pageNum, Integer pageSize) {
        return commonQueryService.page(
                "select ro.id, ro.order_no as orderNo, ro.title, ro.description, ro.status, ro.submitted_at as submittedAt, ro.completed_at as completedAt, " +
                        "rt.type_name as repairTypeName, db.building_name as buildingName, dr.room_no as roomNo, df.facility_name as facilityName, df.facility_type as facilityType, " +
                        "su.username as studentUsername, su.real_name as studentName, su.avatar as studentAvatar, ru.real_name as repairerName " +
                        "from repair_order ro " +
                        "left join repair_type rt on ro.repair_type_id = rt.id " +
                        "left join dorm_building db on ro.building_id = db.id " +
                        "left join dorm_room dr on ro.room_id = dr.id " +
                        "left join dorm_facility df on ro.facility_id = df.id " +
                        "left join user su on ro.student_id = su.id " +
                        "left join user ru on ro.assigned_repairer_id = ru.id " +
                        "order by ro.id desc",
                pageNum,
                pageSize
        );
    }

    public Map<String, Object> repairOrderDetail(Long id) {
        // 公开首页查看工单详情时只展示基础信息和服务结果，不暴露图片和时间线，降低页面复杂度。
        Map<String, Object> detail = commonQueryService.one(
                "select ro.id, ro.order_no as orderNo, ro.title, ro.description, ro.expect_time as expectTime, ro.status, ro.submitted_at as submittedAt, ro.assigned_at as assignedAt, ro.completed_at as completedAt, " +
                        "rt.type_name as repairTypeName, db.building_name as buildingName, dr.room_no as roomNo, df.facility_name as facilityName, df.facility_type as facilityType, " +
                        "su.username as studentUsername, su.real_name as studentName, ru.real_name as repairerName, " +
                        "rf.result_desc as resultDesc, rf.materials_used as materialsUsed, rf.finish_time as finishTime, " +
                        "rr.score as ratingScore, rr.content as ratingContent, rr.created_at as ratedAt " +
                        "from repair_order ro " +
                        "left join repair_type rt on ro.repair_type_id = rt.id " +
                        "left join dorm_building db on ro.building_id = db.id " +
                        "left join dorm_room dr on ro.room_id = dr.id " +
                        "left join dorm_facility df on ro.facility_id = df.id " +
                        "left join user su on ro.student_id = su.id " +
                        "left join user ru on ro.assigned_repairer_id = ru.id " +
                        "left join repair_feedback rf on ro.id = rf.repair_order_id " +
                        "left join repair_rating rr on ro.id = rr.repair_order_id " +
                        "where ro.id = ?",
                id
        );
        return detail;
    }

    public Map<String, Object> schoolOptions() {
        // 注册页使用公开选项接口，保证学生只能选择管理员维护过的学院、专业和班级。
        Map<String, Object> result = new HashMap<>();
        result.put("colleges", commonQueryService.list(
                "select id, college_name as collegeName, sort_no as sortNo from school_college where status = 'enabled' order by sort_no asc, id asc"
        ));
        result.put("majors", commonQueryService.list(
                "select sm.id, sm.college_id as collegeId, sm.major_name as majorName, sc.college_name as collegeName, sm.sort_no as sortNo " +
                        "from school_major sm left join school_college sc on sm.college_id = sc.id " +
                        "where sm.status = 'enabled' and sc.status = 'enabled' order by sc.sort_no asc, sm.sort_no asc, sm.id asc"
        ));
        result.put("classes", commonQueryService.list(
                "select scl.id, scl.major_id as majorId, scl.class_name as className, sm.major_name as majorName, sc.id as collegeId, sc.college_name as collegeName, scl.sort_no as sortNo " +
                        "from school_class scl " +
                        "left join school_major sm on scl.major_id = sm.id " +
                        "left join school_college sc on sm.college_id = sc.id " +
                        "where scl.status = 'enabled' and sm.status = 'enabled' and sc.status = 'enabled' order by sc.sort_no asc, sm.sort_no asc, scl.sort_no asc, scl.id asc"
        ));
        return result;
    }

    public Map<String, Object> forumPosts(String keyword, Integer pageNum, Integer pageSize) {
        // 论坛浏览已收紧为登录后访问，避免首页和匿名接口暴露公开社区内容。
        SecurityUtils.requireRole("student");
        StringBuilder sql = new StringBuilder();
        sql.append("select fp.id, fp.title, fp.content, fp.image_path as imagePath, fp.created_at as createdAt, fp.updated_at as updatedAt, ");
        sql.append("u.username as username, u.real_name as studentName, u.avatar as avatar, ");
        sql.append("0 as commentCount ");
        sql.append("from forum_post fp left join user u on fp.student_id = u.id ");
        sql.append("where fp.status = 'published' ");
        List<Object> args = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            sql.append("and (fp.title like ? or fp.content like ? or u.username like ? or u.real_name like ?) ");
            args.add(likeKeyword);
            args.add(likeKeyword);
            args.add(likeKeyword);
            args.add(likeKeyword);
        }
        sql.append("order by fp.id desc");
        Map<String, Object> page = commonQueryService.page(sql.toString(), pageNum, pageSize, args.toArray());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> posts = (List<Map<String, Object>>) page.get("records");
        for (Map<String, Object> post : posts) {
            Long postId = ((Number) post.get("id")).longValue();
            post.put("comments", forumComments(postId));
            post.put("commentCount", forumCommentCount(postId));
        }
        return page;
    }

    public Map<String, Object> repairers(Integer pageNum, Integer pageSize) {
        return commonQueryService.page(
                "select u.id, u.username, u.real_name as realName, u.phone, u.avatar, u.work_type_code as workTypeCode, " +
                        "(select group_concat(d.dict_name, '、') from sys_dict d where d.dict_type = 'repair_work_type' and instr(',' || coalesce(u.work_type_code, '') || ',', ',' || d.dict_code || ',') > 0) as workTypeName, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id) as totalCount, " +
                        "(select count(*) from repair_order ro where ro.assigned_repairer_id = u.id and ro.status in ('pending_rating', 'completed')) as completedCount, " +
                        "(select round(avg(rr.score), 2) from repair_rating rr left join repair_order ro on rr.repair_order_id = ro.id where ro.assigned_repairer_id = u.id) as avgScore " +
                        "from user u where u.role = 'repairer' and u.status = 'enabled' order by completedCount desc, totalCount desc, u.id asc",
                pageNum,
                pageSize
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
        // 评论属于学生论坛互动内容，必须登录且必须是学生身份。
        SecurityUtils.requireRole("student");
        JwtUser user = SecurityUtils.currentUser();
        Integer postCount = jdbcTemplate.queryForObject("select count(*) from forum_post where id = ? and status = 'published'", Integer.class, postId);
        if (postCount == null || postCount == 0) {
            throw new BusinessException("帖子不存在或已隐藏");
        }
        String content = request.content() == null ? "" : request.content().trim();
        if (content.isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        String now = TimeUtils.now();
        jdbcTemplate.update(
                "insert into forum_comment(forum_post_id, user_id, content, created_at) values (?, ?, ?, ?)",
                postId, user.id(), content, now
        );
        logService.log(user.id(), "论坛评论", "新增", "评论论坛帖子: " + postId + "，内容：" + content);
    }

    private int forumCommentCount(Long postId) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from forum_comment where forum_post_id = ?", Integer.class, postId);
        return count == null ? 0 : count;
    }

    private List<Map<String, Object>> forumComments(Long postId) {
        return commonQueryService.list(
                "select fc.id, fc.content, fc.created_at as createdAt, u.username as username, u.real_name as realName, u.avatar as avatar " +
                        "from forum_comment fc left join user u on fc.user_id = u.id " +
                        "where fc.forum_post_id = ? order by fc.id desc limit 6",
                postId
        );
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
