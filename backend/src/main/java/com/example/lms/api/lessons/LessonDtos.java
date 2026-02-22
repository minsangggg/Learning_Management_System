package com.example.lms.api.lessons;

public class LessonDtos {
  public record LessonResponse(
      long id,
      long courseId,
      String title,
      String content,
      int orderNo,
      String videoUrl,
      Integer startSec,
      Integer endSec) {}
  public record WatchedLessonResponse(
      long lessonId,
      long courseId,
      String courseTitle,
      String lessonTitle,
      String lessonContent,
      double progressPercent) {}
  public record LessonCreateRequest(long courseId, String title, String content, int orderNo) {}
  public record LessonUpdateRequest(String title, String content, int orderNo) {}
}
