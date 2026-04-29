package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import com.dormrepair.dto.auth.ForgotPasswordRequest;
import com.dormrepair.dto.auth.ForgotPasswordQuestionRequest;
import com.dormrepair.dto.auth.LoginRequest;
import com.dormrepair.dto.auth.PasswordRequest;
import com.dormrepair.dto.auth.RegisterRequest;
import com.dormrepair.dto.auth.ResetPasswordRequest;
import com.dormrepair.dto.auth.SecurityQuestionRequest;
import com.dormrepair.security.JwtTokenProvider;
import com.dormrepair.security.JwtUser;
import com.dormrepair.util.SecurityUtils;
import com.dormrepair.util.TimeUtils;
import java.util.HashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final LogService logService;

    public AuthService(JdbcTemplate jdbcTemplate, JwtTokenProvider jwtTokenProvider, LogService logService) {
        this.jdbcTemplate = jdbcTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
        this.logService = logService;
    }

    public Map<String, Object> login(LoginRequest request) {
        Map<String, Object> user = jdbcTemplate.query(
                "select id, username, password, real_name, role, status, phone from user where username = ?",
                rs -> rs.next() ? mapUser(rs.getLong("id"), rs.getString("username"), rs.getString("password"),
                        rs.getString("real_name"), rs.getString("role"), rs.getString("status"), rs.getString("phone")) : null,
                request.username()
        );
        if (user == null || !request.password().equals(user.get("password"))) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!"enabled".equals(user.get("status"))) {
            throw new BusinessException("账号已被禁用");
        }

        JwtUser jwtUser = new JwtUser((Long) user.get("id"), (String) user.get("username"), (String) user.get("role"), (String) user.get("realName"));
        String token = jwtTokenProvider.generateToken(jwtUser);
        logService.log(jwtUser.id(), "认证", "登录", jwtUser.username() + " 登录系统");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", me(jwtUser.id()));
        result.put("role", jwtUser.role());
        return result;
    }

    public void register(RegisterRequest request) {
        Integer count = jdbcTemplate.queryForObject("select count(*) from user where username = ?", Integer.class, request.username());
        if (count != null && count > 0) {
            throw new BusinessException("用户名已存在");
        }
        Integer studentCount = jdbcTemplate.queryForObject("select count(*) from student_profile where student_no = ?", Integer.class, request.studentNo());
        if (studentCount != null && studentCount > 0) {
            throw new BusinessException("学号已存在");
        }

        String now = TimeUtils.now();
        jdbcTemplate.update(
                "insert into user(username, password, real_name, phone, role, password_question, password_answer, status, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                request.username(), request.password(), request.realName(), request.phone(), "student",
                cleanRequired(request.passwordQuestion(), "请设置找回密码问题"),
                cleanRequired(request.passwordAnswer(), "请设置找回密码答案"),
                "enabled", now, now
        );
        Long userId = jdbcTemplate.queryForObject("select id from user where username = ?", Long.class, request.username());
        jdbcTemplate.update(
                "insert into student_profile(user_id, student_no, gender, college, major, class_name, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?)",
                userId, request.studentNo(), null, request.college(), request.major(), request.className(), now, now
        );
        logService.log(userId, "认证", "注册", request.username() + " 注册学生账号");
    }

    public Map<String, Object> forgotPasswordQuestion(ForgotPasswordQuestionRequest request) {
        Map<String, Object> user = findPasswordQuestionUser(request.username());
        validateSelfServiceRole((String) user.get("role"));
        String question = (String) user.get("passwordQuestion");
        if (question == null || question.trim().isEmpty()) {
            throw new BusinessException("该账号尚未设置找回密码问题，请联系管理员处理");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("username", user.get("username"));
        result.put("question", question);
        return result;
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        Map<String, Object> user = findPasswordQuestionUser(request.username());
        validateSelfServiceRole((String) user.get("role"));
        String storedAnswer = (String) user.get("passwordAnswer");
        if (storedAnswer == null || storedAnswer.trim().isEmpty()) {
            throw new BusinessException("该账号尚未设置找回密码答案，请联系管理员处理");
        }
        if (!clean(request.answer()).equals(storedAnswer.trim())) {
            throw new BusinessException("找回密码答案不正确");
        }
        jdbcTemplate.update("update user set password = ?, updated_at = ? where id = ?", request.newPassword(), TimeUtils.now(), user.get("id"));
        logService.log(((Number) user.get("id")).longValue(), "认证", "找回密码", request.username() + " 自助找回密码");
    }

    public Map<String, Object> me(Long userId) {
        Long targetId = userId != null ? userId : SecurityUtils.currentUser().id();
        Map<String, Object> user = jdbcTemplate.query(
                "select u.id, u.username, u.real_name, u.phone, u.role, u.status, sp.student_no, sp.college, sp.major, sp.class_name, sp.building_id, sp.room_id, sp.bed_no " +
                        "from user u left join student_profile sp on u.id = sp.user_id where u.id = ?",
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("username", rs.getString("username"));
                    row.put("realName", rs.getString("real_name"));
                    row.put("phone", rs.getString("phone"));
                    row.put("role", rs.getString("role"));
                    row.put("status", rs.getString("status"));
                    row.put("studentNo", rs.getString("student_no"));
                    row.put("college", rs.getString("college"));
                    row.put("major", rs.getString("major"));
                    row.put("className", rs.getString("class_name"));
                    row.put("buildingId", rs.getObject("building_id"));
                    row.put("roomId", rs.getObject("room_id"));
                    row.put("bedNo", rs.getString("bed_no"));
                    return row;
                },
                targetId
        );
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    public Map<String, Object> securityQuestion() {
        JwtUser user = SecurityUtils.currentUser();
        validateSelfServiceRole(user.role());
        Map<String, Object> row = commonSecurityQuestion(user.id());
        Map<String, Object> result = new HashMap<>();
        result.put("question", row.get("passwordQuestion"));
        result.put("hasAnswer", row.get("passwordAnswer") != null && !((String) row.get("passwordAnswer")).trim().isEmpty());
        return result;
    }

    public void updateSecurityQuestion(SecurityQuestionRequest request) {
        JwtUser user = SecurityUtils.currentUser();
        validateSelfServiceRole(user.role());
        jdbcTemplate.update(
                "update user set password_question = ?, password_answer = ?, updated_at = ? where id = ?",
                cleanRequired(request.question(), "请输入找回密码问题"),
                cleanRequired(request.answer(), "请输入找回密码答案"),
                TimeUtils.now(),
                user.id()
        );
        logService.log(user.id(), "认证", "安全设置", user.username() + " 修改找回密码问题");
    }

    public void changePassword(PasswordRequest request) {
        JwtUser user = SecurityUtils.currentUser();
        String oldPassword = jdbcTemplate.queryForObject("select password from user where id = ?", String.class, user.id());
        if (!request.oldPassword().equals(oldPassword)) {
            throw new BusinessException("旧密码错误");
        }
        jdbcTemplate.update("update user set password = ?, updated_at = ? where id = ?", request.newPassword(), TimeUtils.now(), user.id());
        logService.log(user.id(), "认证", "修改密码", user.username() + " 修改密码");
    }

    public void resetPassword(Long userId, ResetPasswordRequest request) {
        SecurityUtils.requireRole("admin");
        jdbcTemplate.update("update user set password = ?, updated_at = ? where id = ?", request.newPassword(), TimeUtils.now(), userId);
        logService.log(SecurityUtils.currentUser().id(), "用户管理", "重置密码", "重置用户密码: " + userId);
    }

    private Map<String, Object> mapUser(Long id, String username, String password, String realName, String role, String status, String phone) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", id);
        row.put("username", username);
        row.put("password", password);
        row.put("realName", realName);
        row.put("role", role);
        row.put("status", status);
        row.put("phone", phone);
        return row;
    }

    private Map<String, Object> findPasswordQuestionUser(String username) {
        Map<String, Object> user = jdbcTemplate.query(
                "select id, username, role, password_question, password_answer from user where username = ?",
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("username", rs.getString("username"));
                    row.put("role", rs.getString("role"));
                    row.put("passwordQuestion", rs.getString("password_question"));
                    row.put("passwordAnswer", rs.getString("password_answer"));
                    return row;
                },
                username
        );
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        return user;
    }

    private Map<String, Object> commonSecurityQuestion(Long userId) {
        return jdbcTemplate.query(
                "select password_question as passwordQuestion, password_answer as passwordAnswer from user where id = ?",
                rs -> {
                    if (!rs.next()) {
                        throw new BusinessException("用户不存在");
                    }
                    Map<String, Object> row = new HashMap<>();
                    row.put("passwordQuestion", rs.getString("passwordQuestion"));
                    row.put("passwordAnswer", rs.getString("passwordAnswer"));
                    return row;
                },
                userId
        );
    }

    private void validateSelfServiceRole(String role) {
        if (!"student".equals(role) && !"dorm_admin".equals(role) && !"repairer".equals(role)) {
            throw new BusinessException("管理员账号不支持自助找回密码，请联系系统管理员处理");
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private String cleanRequired(String value, String message) {
        String result = clean(value);
        if (result.isEmpty()) {
            throw new BusinessException(message);
        }
        return result;
    }
}

