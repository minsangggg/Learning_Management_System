package com.example.lms.api.enrollments;

import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  public String findStatusById(long id) {
    String status =
        jdbcTemplate.queryForObject(
            "SELECT STATUS FROM ENROLLMENTS WHERE ID = ?",
            String.class,
            id);
    if (status == null) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Enrollment not found");
    }
    return status;
  }

  public Optional<EnrollmentEntity> findByUserAndCourse(long userId, long courseId) {
    List<EnrollmentEntity> rows =
        jdbcTemplate.query(
            """
            SELECT ID, USER_ID, COURSE_ID, STATUS, ENROLLED_AT
            FROM ENROLLMENTS
            WHERE USER_ID = ? AND COURSE_ID = ?
            ORDER BY ENROLLED_AT DESC
            FETCH FIRST 1 ROWS ONLY
            """,
            mapper(),
            userId,
            courseId);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public long enroll(long userId, long courseId) {
    if (!courseExists(courseId)) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Course not found");
    }
    Optional<EnrollmentEntity> existing = findByUserAndCourse(userId, courseId);
    if (existing.isPresent()) {
      return existing.get().id();
    }
    Map<String, Object> params = new HashMap<>();
    params.put("USER_ID", userId);
    params.put("COURSE_ID", courseId);
    params.put("STATUS", "PENDING");
    Number id = insert.executeAndReturnKey(params);
    return id.longValue();
  }

  public List<PendingEnrollmentRow> findPending() {
    return jdbcTemplate.query(
        """
        SELECT
          e.ID AS ENROLLMENT_ID,
          e.USER_ID,
          u.EMAIL AS USER_EMAIL,
          u.NAME AS USER_NAME,
          e.COURSE_ID,
          c.TITLE AS COURSE_TITLE,
          e.STATUS,
          e.ENROLLED_AT
        FROM ENROLLMENTS e
        JOIN USERS u ON u.ID = e.USER_ID
        JOIN COURSES c ON c.ID = e.COURSE_ID
        WHERE e.STATUS = 'PENDING'
        ORDER BY e.ENROLLED_AT DESC
        """,
        pendingMapper());
  }

  public void updateStatus(long enrollmentId, String status) {
    int updated =
        jdbcTemplate.update(
            "UPDATE ENROLLMENTS SET STATUS = ? WHERE ID = ?",
            status,
            enrollmentId);
    if (updated == 0) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Enrollment not found");
    }
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

  private RowMapper<PendingEnrollmentRow> pendingMapper() {
    return (ResultSet rs, int rowNum) ->
        new PendingEnrollmentRow(
            rs.getLong("ENROLLMENT_ID"),
            rs.getLong("USER_ID"),
            rs.getString("USER_EMAIL"),
            rs.getString("USER_NAME"),
            rs.getLong("COURSE_ID"),
            rs.getString("COURSE_TITLE"),
            rs.getString("STATUS"),
            rs.getTimestamp("ENROLLED_AT").toInstant());
  }
}

record EnrollmentEntity(long id, long userId, long courseId, String status, java.time.Instant enrolledAt) {}

record PendingEnrollmentRow(
    long id,
    long userId,
    String userEmail,
    String userName,
    long courseId,
    String courseTitle,
    String status,
    java.time.Instant enrolledAt) {}
