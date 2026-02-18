package com.example.lms.api.lessons;

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
public class LessonRepository {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert insert;

  public LessonRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.insert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("LESSONS")
            .usingGeneratedKeyColumns("ID")
            .usingColumns("COURSE_ID", "TITLE", "CONTENT", "ORDER_NO");
  }

  public List<LessonEntity> findAll() {
    return jdbcTemplate.query(
        "SELECT ID, COURSE_ID, TITLE, CONTENT, ORDER_NO, VIDEO_URL FROM LESSONS ORDER BY ID",
        mapper());
  }

  public List<LessonEntity> findByCourseId(long courseId) {
    return jdbcTemplate.query(
        "SELECT ID, COURSE_ID, TITLE, CONTENT, ORDER_NO, VIDEO_URL FROM LESSONS WHERE COURSE_ID = ? ORDER BY ORDER_NO",
        mapper(),
        courseId);
  }

  public LessonEntity findById(long id) {
    List<LessonEntity> rows =
        jdbcTemplate.query(
            "SELECT ID, COURSE_ID, TITLE, CONTENT, ORDER_NO, VIDEO_URL FROM LESSONS WHERE ID = ?",
            mapper(),
            id);
    if (rows.isEmpty()) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Lesson not found");
    }
    return rows.get(0);
  }

  public List<WatchedLessonRow> findWatchedByUserId(long userId) {
    return jdbcTemplate.query(
        """
        SELECT
          l.ID AS LESSON_ID,
          l.COURSE_ID,
          c.TITLE AS COURSE_TITLE,
          l.TITLE AS LESSON_TITLE,
          l.CONTENT AS LESSON_CONTENT,
          MAX(p.PROGRESS_PERCENT) AS PROGRESS_PERCENT,
          l.ORDER_NO
        FROM PROGRESS p
        JOIN ENROLLMENTS e ON e.ID = p.ENROLLMENT_ID
        JOIN LESSONS l ON l.ID = p.LESSON_ID
        JOIN COURSES c ON c.ID = l.COURSE_ID
        WHERE e.USER_ID = ? AND p.PROGRESS_PERCENT > 0
        GROUP BY l.ID, l.COURSE_ID, c.TITLE, l.TITLE, l.CONTENT, l.ORDER_NO
        ORDER BY c.TITLE, l.ORDER_NO
        """,
        watchedMapper(),
        userId);
  }

  public long insert(long courseId, String title, String content, int orderNo) {
    if (!courseExists(courseId)) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Course not found");
    }
    Map<String, Object> params = new HashMap<>();
    params.put("COURSE_ID", courseId);
    params.put("TITLE", title);
    params.put("CONTENT", content);
    params.put("ORDER_NO", orderNo);
    Number id = insert.executeAndReturnKey(params);
    return id.longValue();
  }

  public void update(long id, String title, String content, int orderNo) {
    int updated =
        jdbcTemplate.update(
            "UPDATE LESSONS SET TITLE = ?, CONTENT = ?, ORDER_NO = ? WHERE ID = ?",
            title,
            content,
            orderNo,
            id);
    if (updated == 0) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Lesson not found");
    }
  }

  public void delete(long id) {
    int deleted = jdbcTemplate.update("DELETE FROM LESSONS WHERE ID = ?", id);
    if (deleted == 0) {
      throw new ApiException(ErrorCode.NOT_FOUND, "Lesson not found");
    }
  }

  private RowMapper<LessonEntity> mapper() {
    return (ResultSet rs, int rowNum) ->
        new LessonEntity(
            rs.getLong("ID"),
            rs.getLong("COURSE_ID"),
            rs.getString("TITLE"),
            rs.getString("CONTENT"),
            rs.getInt("ORDER_NO"),
            rs.getString("VIDEO_URL"));
  }

  private RowMapper<WatchedLessonRow> watchedMapper() {
    return (ResultSet rs, int rowNum) ->
        new WatchedLessonRow(
            rs.getLong("LESSON_ID"),
            rs.getLong("COURSE_ID"),
            rs.getString("COURSE_TITLE"),
            rs.getString("LESSON_TITLE"),
            rs.getString("LESSON_CONTENT"),
            rs.getDouble("PROGRESS_PERCENT"));
  }

  private boolean courseExists(long courseId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM COURSES WHERE ID = ?",
            Integer.class,
            courseId);
    return count != null && count > 0;
  }
}

record LessonEntity(
    long id,
    long courseId,
    String title,
    String content,
    int orderNo,
    String videoUrl) {}

record WatchedLessonRow(
    long lessonId,
    long courseId,
    String courseTitle,
    String lessonTitle,
    String lessonContent,
    double progressPercent) {}
