package com.example.lms.api.guides;

import com.example.lms.api.guides.GuideDtos.GuideResponse;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class GuideRepository {
  private final JdbcTemplate jdbcTemplate;

  public GuideRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<GuideResponse> findGuides(Long userId) {
    String sql =
        """
        SELECT
          g.ID,
          g.USER_ID,
          u.NAME,
          u.EMAIL,
          g.COURSE_ID,
          c.TITLE,
          g.GUIDE_TEXT,
          g.CREATED_AT
        FROM LEARNING_GUIDES g
        JOIN USERS u ON u.ID = g.USER_ID
        JOIN COURSES c ON c.ID = g.COURSE_ID
        WHERE u.ROLE = 'LEARNER'
        """;
    List<Object> params = new ArrayList<>();
    if (userId != null) {
      sql += " AND g.USER_ID = ?";
      params.add(userId);
    }
    sql += " ORDER BY g.CREATED_AT DESC, g.ID DESC";
    return jdbcTemplate.query(sql, mapper(), params.toArray());
  }

  private RowMapper<GuideResponse> mapper() {
    return (ResultSet rs, int rowNum) ->
        new GuideResponse(
            rs.getLong("ID"),
            rs.getLong("USER_ID"),
            rs.getString("NAME"),
            rs.getString("EMAIL"),
            rs.getLong("COURSE_ID"),
            rs.getString("TITLE"),
            rs.getString("GUIDE_TEXT"),
            rs.getTimestamp("CREATED_AT") == null
                ? null
                : rs.getTimestamp("CREATED_AT").toInstant().toString());
  }
}
