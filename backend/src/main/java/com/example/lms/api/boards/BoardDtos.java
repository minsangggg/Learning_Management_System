package com.example.lms.api.boards;

public class BoardDtos {
  public record BoardResponse(long id, String title, String content, String createdAt, String updatedAt) {}

  public record BoardCreateRequest(String title, String content) {}

  public record BoardUpdateRequest(String title, String content) {}
}
