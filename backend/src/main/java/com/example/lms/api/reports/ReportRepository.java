package com.example.lms.api.reports;

import com.example.lms.api.reports.ReportDtos.CourseCompletionReportRow;
import com.example.lms.api.reports.ReportDtos.CoursePeriodReportRow;
import com.example.lms.api.reports.ReportDtos.LearnerProgressRow;
import java.sql.Date;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ReportRepository {
  private final JdbcTemplate jdbcTemplate;

  public ReportRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<CoursePeriodReportRow> coursePeriodReport(Date from, Date to, Long courseId) {
    String baseSql =
        """
        SELECT
          c.ID AS COURSE_ID,
          c.TITLE AS COURSE_TITLE,
          COUNT(DISTINCT e.USER_ID) AS LEARNER_COUNT,
          NVL(AVG(p.PROGRESS_PERCENT), 0) AS AVG_PROGRESS
        FROM COURSES c
        JOIN ENROLLMENTS e ON e.COURSE_ID = c.ID
        LEFT JOIN PROGRESS p ON p.ENROLLMENT_ID = e.ID
        WHERE e.ENROLLED_AT >= ? AND e.ENROLLED_AT < (? + 1)
        """;

    List<Object> params = new ArrayList<>();
    params.add(from);
    params.add(to);

    if (courseId != null) {
      baseSql += " AND c.ID = ?";
      params.add(courseId);
    }

    baseSql += " GROUP BY c.ID, c.TITLE ORDER BY c.ID";

    return jdbcTemplate.query(baseSql, mapperPeriod(), params.toArray());
  }

  public List<CourseCompletionReportRow> courseCompletionReport(Date from, Date to, Long courseId) {
    String baseSql =
        """
        SELECT
          c.ID AS COURSE_ID,
          c.TITLE AS COURSE_TITLE,
          COUNT(DISTINCT CASE WHEN p.PROGRESS_PERCENT = 100 THEN e.USER_ID END) AS COMPLETED_LEARNERS
        FROM COURSES c
        JOIN ENROLLMENTS e ON e.COURSE_ID = c.ID
        LEFT JOIN PROGRESS p ON p.ENROLLMENT_ID = e.ID
        WHERE e.ENROLLED_AT >= ? AND e.ENROLLED_AT < (? + 1)
        """;

    List<Object> params = new ArrayList<>();
    params.add(from);
    params.add(to);

    if (courseId != null) {
      baseSql += " AND c.ID = ?";
      params.add(courseId);
    }

    baseSql += " GROUP BY c.ID, c.TITLE ORDER BY c.ID";

    return jdbcTemplate.query(baseSql, mapperCompletion(), params.toArray());
  }

  public List<LearnerProgressRow> learnerProgress(Long userId) {
    String sql =
        """
        SELECT
          u.ID AS USER_ID,
          u.NAME AS USER_NAME,
          c.ID AS COURSE_ID,
          c.TITLE AS COURSE_TITLE,
          NVL(AVG(p.PROGRESS_PERCENT), 0) AS AVG_PROGRESS,
          SUM(CASE WHEN p.PROGRESS_PERCENT = 100 THEN 1 ELSE 0 END) AS COMPLETED_LESSONS,
          COUNT(l.ID) AS TOTAL_LESSONS
        FROM USERS u
        JOIN ENROLLMENTS e ON e.USER_ID = u.ID
        JOIN COURSES c ON c.ID = e.COURSE_ID
        JOIN LESSONS l ON l.COURSE_ID = c.ID
        LEFT JOIN PROGRESS p ON p.ENROLLMENT_ID = e.ID AND p.LESSON_ID = l.ID
        WHERE u.ROLE = 'LEARNER'
        """;
    List<Object> params = new ArrayList<>();
    if (userId != null) {
      sql += " AND u.ID = ?";
      params.add(userId);
    }
    sql += " GROUP BY u.ID, u.NAME, c.ID, c.TITLE ORDER BY u.ID, c.ID";
    return jdbcTemplate.query(sql, mapperLearnerProgress(), params.toArray());
  }

  private RowMapper<CoursePeriodReportRow> mapperPeriod() {
    return (ResultSet rs, int rowNum) ->
        new CoursePeriodReportRow(
            rs.getLong("COURSE_ID"),
            rs.getString("COURSE_TITLE"),
            rs.getLong("LEARNER_COUNT"),
            rs.getDouble("AVG_PROGRESS"));
  }

  private RowMapper<CourseCompletionReportRow> mapperCompletion() {
    return (ResultSet rs, int rowNum) ->
        new CourseCompletionReportRow(
            rs.getLong("COURSE_ID"),
            rs.getString("COURSE_TITLE"),
            rs.getLong("COMPLETED_LEARNERS"));
  }

  private RowMapper<LearnerProgressRow> mapperLearnerProgress() {
    return (ResultSet rs, int rowNum) ->
        new LearnerProgressRow(
            rs.getLong("USER_ID"),
            rs.getString("USER_NAME"),
            rs.getLong("COURSE_ID"),
            rs.getString("COURSE_TITLE"),
            rs.getDouble("AVG_PROGRESS"),
            rs.getLong("COMPLETED_LESSONS"),
            rs.getLong("TOTAL_LESSONS"));
  }
}
