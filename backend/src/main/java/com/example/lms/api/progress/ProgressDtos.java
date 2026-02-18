package com.example.lms.api.progress;

public class ProgressDtos {
  public record ProgressResponse(
      long id,
      long enrollmentId,
      long lessonId,
      double progressPercent,
      String completedAt) {}

  public record ProgressUpdateRequest(long enrollmentId, long lessonId, double progressPercent) {}
}
