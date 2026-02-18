package com.example.lms.api.enrollments;

import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class EnrollmentRepository {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert insert;

  public EnrollmentRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.insert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("ENROLLMENTS")
            .usingGeneratedKeyColumns("ID")
            .usingColumns("USER_ID", "COURSE_ID", "STATUS");
  }

  public EnrollmentEntity findById(long id) {
    List<EnrollmentEntity> rows =
        jdbcTemplate.query(
            "SELECT ID, USER_ID, COURSE_ID, STATUS, ENROLLED_AT FROM ENROLLMENTS WHERE ID = ?",
            mapper(),
            id);
    if (rows.isEmpty()) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Enrollment not found");
    }
    return rows.get(0);
  }

  public long enroll(long userId, long courseId) {
    if (!courseExists(courseId)) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Course not found");
    }
    Map<String, Object> params = new HashMap<>();
    params.put("USER_ID", userId);
    params.put("COURSE_ID", courseId);
    params.put("STATUS", "ENROLLED");
    Number id = insert.executeAndReturnKey(params);
    return id.longValue();
  }

  public void requireOwnedByUser(long enrollmentId, long userId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM ENROLLMENTS WHERE ID = ? AND USER_ID = ?",
            Integer.class,
            enrollmentId,
            userId);
    if (count == null || count == 0) {
      throw new ApiException(ErrorCode.FORBIDDEN, "Enrollment does not belong to user");
    }
  }

  private boolean courseExists(long courseId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM COURSES WHERE ID = ?",
            Integer.class,
            courseId);
    return count != null && count > 0;
  }

  private RowMapper<EnrollmentEntity> mapper() {
    return (ResultSet rs, int rowNum) ->
        new EnrollmentEntity(
            rs.getLong("ID"),
            rs.getLong("USER_ID"),
            rs.getLong("COURSE_ID"),
            rs.getString("STATUS"),
            rs.getTimestamp("ENROLLED_AT").toInstant());
  }
}

record EnrollmentEntity(long id, long userId, long courseId, String status, java.time.Instant enrolledAt) {}
