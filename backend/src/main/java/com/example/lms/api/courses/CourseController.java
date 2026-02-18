package com.example.lms.api.courses;

import com.example.lms.api.courses.CourseDtos.CourseCreateRequest;
import com.example.lms.api.courses.CourseDtos.CourseResponse;
import com.example.lms.api.courses.CourseDtos.CourseUpdateRequest;
import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import com.example.lms.api.security.AuthContext;
import com.example.lms.api.security.AuthUtil;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
  private final CourseRepository repository;

  public CourseController(CourseRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<CourseResponse> list(
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    return repository.findAll().stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public CourseResponse detail(
      @PathVariable("id") long id,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    return toResponse(repository.findById(id));
  }

  @PostMapping
  public CourseResponse create(
      @RequestBody CourseCreateRequest request,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireAdmin(ctx);
    validateTitle(request.title());
    long id = repository.insert(request.title(), request.description());
    return toResponse(repository.findById(id));
  }

  @PutMapping("/{id}")
  public CourseResponse update(
      @PathVariable("id") long id,
      @RequestBody CourseUpdateRequest request,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireAdmin(ctx);
    validateTitle(request.title());
    repository.update(id, request.title(), request.description());
    return toResponse(repository.findById(id));
  }

  @DeleteMapping("/{id}")
  public void delete(
      @PathVariable("id") long id,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireAdmin(ctx);
    repository.delete(id);
  }

  private CourseResponse toResponse(CourseEntity entity) {
    return new CourseResponse(
        entity.id(),
        entity.title(),
        entity.description(),
        entity.createdAt().toString());
  }

  private void validateTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new ApiException(ErrorCode.VALIDATION_ERROR, "Title is required");
    }
  }
}
