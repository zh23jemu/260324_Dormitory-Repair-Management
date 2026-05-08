package com.dormrepair.service;

import com.dormrepair.common.BusinessException;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
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

    public Map<String, Object> page(String sql, Integer pageNum, Integer pageSize, Object... args) {
        // 所有列表页统一走后端分页，避免前端一次性加载过多记录导致页面卡顿或排版被撑开。
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        int offset = (safePageNum - 1) * safePageSize;

        Integer total = jdbcTemplate.queryForObject("select count(*) from (" + sql + ") page_count", Integer.class, args);
        List<Object> pageArgs = new ArrayList<>(List.of(args));
        pageArgs.add(safePageSize);
        pageArgs.add(offset);

        Map<String, Object> result = new HashMap<>();
        result.put("records", list(sql + " limit ? offset ?", pageArgs.toArray()));
        result.put("total", total == null ? 0 : total);
        result.put("pageNum", safePageNum);
        result.put("pageSize", safePageSize);
        result.put("pages", total == null || total == 0 ? 0 : (int) Math.ceil(total / (double) safePageSize));
        return result;
    }

    public Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = list(sql, args);
        if (rows.isEmpty()) {
            throw new BusinessException("数据不存在");
        }
        return rows.get(0);
    }
}
