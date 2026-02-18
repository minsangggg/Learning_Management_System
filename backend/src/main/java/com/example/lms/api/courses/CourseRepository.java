package com.example.lms.api.courses;

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
public class CourseRepository {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert insert;

  public CourseRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.insert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("COURSES")
            .usingGeneratedKeyColumns("ID")
            .usingColumns("TITLE", "DESCRIPTION");
  }

  public List<CourseEntity> findAll() {
    return jdbcTemplate.query("SELECT ID, TITLE, DESCRIPTION, CREATED_AT FROM COURSES ORDER BY ID", mapper());
  }

  public CourseEntity findById(long id) {
    List<CourseEntity> rows =
        jdbcTemplate.query(
            "SELECT ID, TITLE, DESCRIPTION, CREATED_AT FROM COURSES WHERE ID = ?",
            mapper(),
            id);
    if (rows.isEmpty()) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Course not found");
    }
    return rows.get(0);
  }

  public long insert(String title, String description) {
    Map<String, Object> params = new HashMap<>();
    params.put("TITLE", title);
    params.put("DESCRIPTION", description);
    Number id = insert.executeAndReturnKey(params);
    return id.longValue();
  }

  public void update(long id, String title, String description) {
    int updated =
        jdbcTemplate.update(
            "UPDATE COURSES SET TITLE = ?, DESCRIPTION = ? WHERE ID = ?",
            title,
            description,
            id);
    if (updated == 0) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Course not found");
    }
  }

  public void delete(long id) {
    int deleted = jdbcTemplate.update("DELETE FROM COURSES WHERE ID = ?", id);
    if (deleted == 0) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Course not found");
    }
  }

  private RowMapper<CourseEntity> mapper() {
    return (ResultSet rs, int rowNum) ->
        new CourseEntity(
            rs.getLong("ID"),
            rs.getString("TITLE"),
            rs.getString("DESCRIPTION"),
            rs.getTimestamp("CREATED_AT").toInstant());
  }
}

record CourseEntity(long id, String title, String description, Instant createdAt) {}
