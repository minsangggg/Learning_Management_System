package com.example.lms.api.boards;

import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class BoardRepository {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert insert;

  public BoardRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.insert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("BOARDS")
            .usingGeneratedKeyColumns("ID")
            .usingColumns("TITLE", "CONTENT", "CREATED_AT", "UPDATED_AT");
  }

  public List<BoardEntity> findAll() {
    return jdbcTemplate.query(
        "SELECT ID, TITLE, CONTENT, CREATED_AT, UPDATED_AT FROM BOARDS ORDER BY CREATED_AT DESC, ID DESC",
        mapper());
  }

  public BoardEntity findById(long id) {
    List<BoardEntity> rows =
        jdbcTemplate.query(
            "SELECT ID, TITLE, CONTENT, CREATED_AT, UPDATED_AT FROM BOARDS WHERE ID = ?",
            mapper(),
            id);
    if (rows.isEmpty()) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Board not found");
    }
    return rows.get(0);
  }

  public long insert(String title, String content) {
    Map<String, Object> params = new HashMap<>();
    params.put("TITLE", title);
    params.put("CONTENT", content);
    params.put("CREATED_AT", java.sql.Timestamp.from(Instant.now()));
    params.put("UPDATED_AT", java.sql.Timestamp.from(Instant.now()));
    Number id = insert.executeAndReturnKey(params);
    return id.longValue();
  }

  public void update(long id, String title, String content) {
    int updated =
        jdbcTemplate.update(
            "UPDATE BOARDS SET TITLE = ?, CONTENT = ?, UPDATED_AT = SYSTIMESTAMP WHERE ID = ?",
            title,
            content,
            id);
    if (updated == 0) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Board not found");
    }
  }

  public void delete(long id) {
    int deleted = jdbcTemplate.update("DELETE FROM BOARDS WHERE ID = ?", id);
    if (deleted == 0) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Board not found");
    }
  }

  private RowMapper<BoardEntity> mapper() {
    return (ResultSet rs, int rowNum) ->
        new BoardEntity(
            rs.getLong("ID"),
            rs.getString("TITLE"),
            rs.getString("CONTENT"),
            rs.getTimestamp("CREATED_AT") == null ? null : rs.getTimestamp("CREATED_AT").toInstant(),
            rs.getTimestamp("UPDATED_AT") == null ? null : rs.getTimestamp("UPDATED_AT").toInstant());
  }
}

record BoardEntity(long id, String title, String content, Instant createdAt, Instant updatedAt) {}
