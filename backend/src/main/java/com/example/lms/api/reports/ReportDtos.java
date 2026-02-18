package com.example.lms.api.reports;

public class ReportDtos {
  public record CoursePeriodReportRow(
      long courseId, String courseTitle, long learnerCount, double avgProgress) {}

  public record CourseCompletionReportRow(
      long courseId, String courseTitle, long completedLearners) {}

  public record LearnerProgressRow(
      long userId,
      String userName,
      long courseId,
      String courseTitle,
      double avgProgress,
      long completedLessons,
      long totalLessons) {}
}
