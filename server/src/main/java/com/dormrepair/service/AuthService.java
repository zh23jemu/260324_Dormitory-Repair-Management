package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import com.dormrepair.dto.auth.ForgotPasswordRequest;
import com.dormrepair.dto.auth.LoginRequest;
import com.dormrepair.dto.auth.PasswordRequest;
import com.dormrepair.dto.auth.RegisterRequest;
import com.dormrepair.dto.auth.ResetPasswordRequest;
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
                "insert into user(username, password, real_name, phone, role, status, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?)",
                request.username(), request.password(), request.realName(), request.phone(), "student", "enabled", now, now
        );
        Long userId = jdbcTemplate.queryForObject("select id from user where username = ?", Long.class, request.username());
        jdbcTemplate.update(
                "insert into student_profile(user_id, student_no, gender, college, major, class_name, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?)",
                userId, request.studentNo(), null, request.college(), request.major(), request.className(), now, now
        );
        logService.log(userId, "认证", "注册", request.username() + " 注册学生账号");
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        Map<String, Object> user = jdbcTemplate.query(
                "select u.id, u.username, u.role, u.phone, sp.student_no from user u left join student_profile sp on u.id = sp.user_id where u.username = ?",
                rs -> {
                    if (!rs.next()) {
                        return null;
                    }
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("username", rs.getString("username"));
                    row.put("role", rs.getString("role"));
                    row.put("phone", rs.getString("phone"));
                    row.put("studentNo", rs.getString("student_no"));
                    return row;
                },
                request.username()
        );
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        if (!"student".equals(user.get("role"))) {
            throw new BusinessException("仅学生账号支持自助找回密码");
        }
        if (!request.studentNo().equals(user.get("studentNo")) || !request.phone().equals(user.get("phone"))) {
            throw new BusinessException("账号、学号或手机号不匹配");
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
}

