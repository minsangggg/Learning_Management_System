package com.example.lms.api.enrollments;

public class EnrollmentDtos {
  public record EnrollRequest(long courseId) {}
  public record EnrollmentResponse(long id, long userId, long courseId, String status, String enrolledAt) {}
  public record PendingEnrollmentResponse(
      long id,
      long userId,
      String userEmail,
      String userName,
      long courseId,
      String courseTitle,
      String status,
      String enrolledAt) {}
}
