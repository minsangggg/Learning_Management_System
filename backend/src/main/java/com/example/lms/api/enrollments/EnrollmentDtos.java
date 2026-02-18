package com.example.lms.api.enrollments;

public class EnrollmentDtos {
  public record EnrollRequest(long courseId) {}
  public record EnrollmentResponse(long id, long userId, long courseId, String status, String enrolledAt) {}
}
