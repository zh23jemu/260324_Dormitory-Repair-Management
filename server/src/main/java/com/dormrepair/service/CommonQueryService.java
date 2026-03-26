package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommonQueryService {

    private final JdbcTemplate jdbcTemplate;

    public CommonQueryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> list(String sql, Object... args) {
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ResultSetMetaData metaData = rs.getMetaData();
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            return row;
        }, args);
    }

    public Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = list(sql, args);
        if (rows.isEmpty()) {
            throw new BusinessException("数据不存在");
        }
        return rows.get(0);
    }
}
