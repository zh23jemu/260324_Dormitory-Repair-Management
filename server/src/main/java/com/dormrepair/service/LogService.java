package com.dormrepair.service;

import com.dormrepair.util.TimeUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private final JdbcTemplate jdbcTemplate;

    public LogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(Long userId, String moduleName, String operationType, String operationDesc) {
        jdbcTemplate.update(
                "insert into sys_log(user_id, module_name, operation_type, operation_desc, ip, created_at) values (?, ?, ?, ?, ?, ?)",
                userId,
                moduleName,
                operationType,
                operationDesc,
                "127.0.0.1",
                TimeUtils.now()
        );
    }
}
