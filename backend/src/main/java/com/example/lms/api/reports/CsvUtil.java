package com.example.lms.api.reports;

import java.util.List;

public final class CsvUtil {
  private CsvUtil() {}

  public static String toCoursePeriodCsv(List<ReportDtos.CoursePeriodReportRow> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append("course_id,course_title,learner_count,avg_progress").append("\n");
    for (ReportDtos.CoursePeriodReportRow row : rows) {
      sb.append(row.courseId()).append(",");
      sb.append(escape(row.courseTitle())).append(",");
      sb.append(row.learnerCount()).append(",");
      sb.append(row.avgProgress()).append("\n");
    }
    return sb.toString();
  }

  public static String toCourseCompletionCsv(List<ReportDtos.CourseCompletionReportRow> rows) {
    StringBuilder sb = new StringBuilder();
    sb.append("course_id,course_title,completed_learners").append("\n");
    for (ReportDtos.CourseCompletionReportRow row : rows) {
      sb.append(row.courseId()).append(",");
      sb.append(escape(row.courseTitle())).append(",");
      sb.append(row.completedLearners()).append("\n");
    }
    return sb.toString();
  }

  private static String escape(String value) {
    if (value == null) {
      return "";
    }
    String escaped = value.replace("\"", "\"\"");
    if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
      return "\"" + escaped + "\"";
    }
    return escaped;
  }
}
