package com.example.lms.api.security;

import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
  private final JdbcTemplate jdbcTemplate;
  private final SimpleJdbcInsert insert;

  public UserRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.insert =
        new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("USERS")
            .usingGeneratedKeyColumns("ID")
            .usingColumns("EMAIL", "PASSWORD_HASH", "ROLE", "NAME", "COMPANY");
  }

  public Optional<UserEntity> findByEmail(String email) {
    List<UserEntity> rows =
        jdbcTemplate.query("SELECT * FROM USERS WHERE EMAIL = ?", mapper(), email);
    return rows.stream().findFirst();
  }

  public Optional<UserEntity> findById(long id) {
    List<UserEntity> rows =
        jdbcTemplate.query("SELECT * FROM USERS WHERE ID = ?", mapper(), id);
    return rows.stream().findFirst();
  }

  public long insert(String email, String passwordHash, String role, String name, String company) {
    Map<String, Object> params = new HashMap<>();
    params.put("EMAIL", email);
    params.put("PASSWORD_HASH", passwordHash);
    params.put("ROLE", role);
    params.put("NAME", name);
    params.put("COMPANY", company);
    try {
      Number id = insert.executeAndReturnKey(params);
      return id.longValue();
    } catch (Exception ex) {
      throw new ApiException(ErrorCode.VALIDATION_ERROR, "Email already exists");
    }
  }

  private RowMapper<UserEntity> mapper() {
    return new RowMapper<>() {
      @Override
      public UserEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new UserEntity(
            rs.getLong("ID"),
            rs.getString("EMAIL"),
            rs.getString("PASSWORD_HASH"),
            rs.getString("ROLE"),
            rs.getString("NAME"),
            rs.getString("COMPANY"),
            rs.getTimestamp("CREATED_AT").toInstant());
      }
    };
  }
}
