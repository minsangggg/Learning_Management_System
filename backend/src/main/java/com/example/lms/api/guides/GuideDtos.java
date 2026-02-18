package com.example.lms.api.guides;

public class GuideDtos {
  public record GuideResponse(
      long id,
      long userId,
      String userName,
      String userEmail,
      long courseId,
      String courseTitle,
      String guideText,
      String createdAt) {}
}
