package com.example.lms.api.reports;

import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import com.example.lms.api.reports.ReportDtos.CourseCompletionReportRow;
import com.example.lms.api.reports.ReportDtos.CoursePeriodReportRow;
import com.example.lms.api.reports.ReportDtos.LearnerProgressRow;
import com.example.lms.api.security.AuthContext;
import com.example.lms.api.security.AuthUtil;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
  private final ReportRepository repository;

  public ReportController(ReportRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/course-period")
  public List<CoursePeriodReportRow> coursePeriod(
      @RequestParam("from") String from,
      @RequestParam("to") String to,
      @RequestParam(name = "courseId", required = false) Long courseId,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireAdmin(ctx);
    Date fromDate = parseDate(from, "from");
    Date toDate = parseDate(to, "to");
    return repository.coursePeriodReport(fromDate, toDate, courseId);
  }

  @GetMapping("/course-period.csv")
  public ResponseEntity<String> coursePeriodCsv(
      @RequestParam("from") String from,
      @RequestParam("to") String to,
      @RequestParam(name = "courseId", required = false) Long courseId,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireAdmin(ctx);
    Date fromDate = parseDate(from, "from");
    Date toDate = parseDate(to, "to");
    List<CoursePeriodReportRow> rows = repository.coursePeriodReport(fromDate, toDate, courseId);
    String csv = CsvUtil.toCoursePeriodCsv(rows);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"course-period.csv\"")
        .contentType(MediaType.valueOf("text/csv"))
        .body(csv);
  }

  @GetMapping("/course-completion")
  public List<CourseCompletionReportRow> courseCompletion(
      @RequestParam("from") String from,
      @RequestParam("to") String to,
      @RequestParam(name = "courseId", required = false) Long courseId,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireAdmin(ctx);
    Date fromDate = parseDate(from, "from");
    Date toDate = parseDate(to, "to");
    return repository.courseCompletionReport(fromDate, toDate, courseId);
  }

  @GetMapping("/learner-progress")
  public List<LearnerProgressRow> learnerProgress(
      @RequestParam(name = "userId", required = false) Long userId,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    if ("ADMIN".equalsIgnoreCase(ctx.role())) {
      return repository.learnerProgress(userId);
    }
    if ("LEARNER".equalsIgnoreCase(ctx.role())) {
      return repository.learnerProgress(ctx.userId());
    }
    throw new ApiException(ErrorCode.FORBIDDEN, "Unsupported role");
  }

  @GetMapping("/course-completion.csv")
  public ResponseEntity<String> courseCompletionCsv(
      @RequestParam("from") String from,
      @RequestParam("to") String to,
      @RequestParam(name = "courseId", required = false) Long courseId,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireAdmin(ctx);
    Date fromDate = parseDate(from, "from");
    Date toDate = parseDate(to, "to");
    List<CourseCompletionReportRow> rows = repository.courseCompletionReport(fromDate, toDate, courseId);
    String csv = CsvUtil.toCourseCompletionCsv(rows);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"course-completion.csv\"")
        .contentType(MediaType.valueOf("text/csv"))
        .body(csv);
  }

  private Date parseDate(String value, String field) {
    try {
      LocalDate date = LocalDate.parse(value);
      return Date.valueOf(date);
    } catch (Exception ex) {
      throw new ApiException(ErrorCode.VALIDATION_ERROR, "Invalid date for " + field + " (yyyy-MM-dd)");
    }
  }
}
