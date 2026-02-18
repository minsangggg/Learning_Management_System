package com.example.lms.api.boards;

import com.example.lms.api.boards.BoardDtos.BoardCreateRequest;
import com.example.lms.api.boards.BoardDtos.BoardResponse;
import com.example.lms.api.boards.BoardDtos.BoardUpdateRequest;
import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
  private final BoardRepository repository;

  public BoardController(BoardRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<BoardResponse> list() {
    return repository.findAll().stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public BoardResponse detail(@PathVariable("id") long id) {
    return toResponse(repository.findById(id));
  }

  @PostMapping
  public BoardResponse create(@RequestBody BoardCreateRequest request) {
    validateTitle(request.title());
    long id = repository.insert(request.title(), request.content());
    return toResponse(repository.findById(id));
  }

  @PutMapping("/{id}")
  public BoardResponse update(@PathVariable("id") long id, @RequestBody BoardUpdateRequest request) {
    validateTitle(request.title());
    repository.update(id, request.title(), request.content());
    return toResponse(repository.findById(id));
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable("id") long id) {
    repository.delete(id);
  }

  private BoardResponse toResponse(BoardEntity entity) {
    return new BoardResponse(
        entity.id(),
        entity.title(),
        entity.content(),
        entity.createdAt() == null ? null : entity.createdAt().toString(),
        entity.updatedAt() == null ? null : entity.updatedAt().toString());
  }

  private void validateTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new ApiException(ErrorCode.VALIDATION_ERROR, "Title is required");
    }
  }
}
