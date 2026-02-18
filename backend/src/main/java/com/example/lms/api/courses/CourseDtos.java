package com.example.lms.api.courses;

public class CourseDtos {
  public record CourseResponse(long id, String title, String description, String createdAt) {}
  public record CourseCreateRequest(String title, String description) {}
  public record CourseUpdateRequest(String title, String description) {}
}
