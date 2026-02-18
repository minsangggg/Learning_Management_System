package com.example.lms.api.progress;

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
public class ProgressRepository {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert insert;

  public ProgressRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.insert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("PROGRESS")
            .usingGeneratedKeyColumns("ID")
            .usingColumns("ENROLLMENT_ID", "LESSON_ID", "PROGRESS_PERCENT", "COMPLETED_AT");
  }

  public List<ProgressEntity> findByEnrollmentId(long enrollmentId) {
    return jdbcTemplate.query(
        "SELECT ID, ENROLLMENT_ID, LESSON_ID, PROGRESS_PERCENT, COMPLETED_AT FROM PROGRESS WHERE ENROLLMENT_ID = ? ORDER BY ID",
        mapper(),
        enrollmentId);
  }

  public long upsert(long enrollmentId, long lessonId, double progressPercent, boolean completed) {
    List<Long> ids =
        jdbcTemplate.query(
            "SELECT ID FROM PROGRESS WHERE ENROLLMENT_ID = ? AND LESSON_ID = ?",
            (rs, rowNum) -> rs.getLong("ID"),
            enrollmentId,
            lessonId);
    if (ids.isEmpty()) {
      Map<String, Object> params = new HashMap<>();
      params.put("ENROLLMENT_ID", enrollmentId);
      params.put("LESSON_ID", lessonId);
      params.put("PROGRESS_PERCENT", progressPercent);
      params.put("COMPLETED_AT", completed ? java.sql.Timestamp.from(Instant.now()) : null);
      Number id = insert.executeAndReturnKey(params);
      return id.longValue();
    }

    long id = ids.get(0);
    jdbcTemplate.update(
        "UPDATE PROGRESS SET PROGRESS_PERCENT = ?, COMPLETED_AT = ? WHERE ID = ?",
        progressPercent,
        completed ? java.sql.Timestamp.from(Instant.now()) : null,
        id);
    return id;
  }

  public ProgressEntity findById(long id) {
    List<ProgressEntity> rows =
        jdbcTemplate.query(
            "SELECT ID, ENROLLMENT_ID, LESSON_ID, PROGRESS_PERCENT, COMPLETED_AT FROM PROGRESS WHERE ID = ?",
            mapper(),
            id);
    if (rows.isEmpty()) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Progress not found");
    }
    return rows.get(0);
  }

  private RowMapper<ProgressEntity> mapper() {
    return (ResultSet rs, int rowNum) ->
        new ProgressEntity(
            rs.getLong("ID"),
            rs.getLong("ENROLLMENT_ID"),
            rs.getLong("LESSON_ID"),
            rs.getDouble("PROGRESS_PERCENT"),
            rs.getTimestamp("COMPLETED_AT") == null
                ? null
                : rs.getTimestamp("COMPLETED_AT").toInstant());
  }
}

record ProgressEntity(
    long id, long enrollmentId, long lessonId, double progressPercent, Instant completedAt) {}
