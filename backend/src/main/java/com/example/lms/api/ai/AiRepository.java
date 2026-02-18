package com.example.lms.api.ai;

import com.example.lms.api.ai.AiDtos.AiRequestLogResponse;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AiRepository {
  private final JdbcTemplate jdbcTemplate;

  public AiRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void insert(
      Long userId,
      String modelName,
      String requestText,
      String responseText,
      long latencyMs,
      String status) {
    jdbcTemplate.update(
        "INSERT INTO AI_REQUESTS (USER_ID, MODEL_NAME, REQUEST_TEXT, RESPONSE_TEXT, LATENCY_MS, STATUS, CREATED_AT) " +
            "VALUES (?, ?, ?, ?, ?, ?, SYSTIMESTAMP)",
        userId,
        modelName,
        requestText,
        responseText,
        latencyMs,
        status);
  }

  public List<AiRequestLogResponse> findRequests(Long userId) {
    String sql =
        """
        SELECT
          a.ID,
          a.USER_ID,
          u.NAME,
          u.EMAIL,
          a.MODEL_NAME,
          a.REQUEST_TEXT,
          a.RESPONSE_TEXT,
          a.LATENCY_MS,
          a.STATUS,
          a.CREATED_AT
        FROM AI_REQUESTS a
        LEFT JOIN USERS u ON u.ID = a.USER_ID
        """;
    List<Object> params = new ArrayList<>();
    if (userId != null) {
      sql += " WHERE a.USER_ID = ?";
      params.add(userId);
    }
    sql += " ORDER BY a.CREATED_AT DESC, a.ID DESC";
    return jdbcTemplate.query(sql, mapper(), params.toArray());
  }

  private RowMapper<AiRequestLogResponse> mapper() {
    return (ResultSet rs, int rowNum) ->
        new AiRequestLogResponse(
            rs.getLong("ID"),
            rs.getLong("USER_ID"),
            rs.getString("NAME"),
            rs.getString("EMAIL"),
            rs.getString("MODEL_NAME"),
            rs.getString("REQUEST_TEXT"),
            rs.getString("RESPONSE_TEXT"),
            rs.getLong("LATENCY_MS"),
            rs.getString("STATUS"),
            rs.getTimestamp("CREATED_AT") == null
                ? null
                : rs.getTimestamp("CREATED_AT").toInstant().toString());
  }
}
