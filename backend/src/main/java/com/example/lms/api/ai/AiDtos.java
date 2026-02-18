package com.example.lms.api.ai;

public class AiDtos {
  public record AiRequest(String text) {}
  public record AiResponse(String output, String status, long latencyMs) {}
  public record AiRequestLogResponse(
      long id,
      long userId,
      String userName,
      String userEmail,
      String modelName,
      String requestText,
      String responseText,
      long latencyMs,
      String status,
      String createdAt) {}
}
