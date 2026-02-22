package com.example.lms.api.lessons;

import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import com.example.lms.api.lessons.LessonDtos.LessonCreateRequest;
import com.example.lms.api.lessons.LessonDtos.LessonResponse;
import com.example.lms.api.lessons.LessonDtos.LessonUpdateRequest;
import com.example.lms.api.lessons.LessonDtos.WatchedLessonResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {
  private final LessonRepository repository;

  public LessonController(LessonRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<LessonResponse> list(
      @RequestParam(name = "courseId", required = false) Long courseId,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireAdmin(ctx);
    List<LessonEntity> rows = courseId == null ? repository.findAll() : repository.findByCourseId(courseId);
    return rows.stream().map(this::toResponse).toList();
  }

  @GetMapping("/public")
  public List<LessonResponse> listForLearner(@RequestParam(name = "courseId") Long courseId) {
    List<LessonEntity> rows = repository.findByCourseId(courseId);
    return rows.stream().map(this::toResponse).toList();
  }

  @GetMapping("/watched")
  public List<WatchedLessonResponse> listWatched(
      @RequestParam(name = "userId", required = false) Long userId,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    boolean isAdmin = "ADMIN".equalsIgnoreCase(ctx.role());
    if (!isAdmin) {
      AuthUtil.requireLearner(ctx);
    }
    long targetUserId = isAdmin && userId != null ? userId : ctx.userId();
    return repository.findWatchedByUserId(targetUserId).stream()
        .map(this::toWatchedResponse)
        .toList();
  }

  @GetMapping("/{id}")
  public LessonResponse detail(
      @PathVariable("id") long id,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireAdmin(ctx);
    return toResponse(repository.findById(id));
  }

  @PostMapping
  public LessonResponse create(
      @RequestBody LessonCreateRequest request,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireAdmin(ctx);
    validateCreate(request);
    long id =
        repository.insert(
            request.courseId(),
            request.title(),
            request.content(),
            request.orderNo());
    return toResponse(repository.findById(id));
  }

  @PutMapping("/{id}")
  public LessonResponse update(
      @PathVariable("id") long id,
      @RequestBody LessonUpdateRequest request,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireAdmin(ctx);
    validateUpdate(request);
    repository.update(id, request.title(), request.content(), request.orderNo());
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

  private LessonResponse toResponse(LessonEntity entity) {
    return new LessonResponse(
        entity.id(),
        entity.courseId(),
        entity.title(),
        entity.content(),
        entity.orderNo(),
        entity.videoUrl(),
        entity.startSec(),
        entity.endSec());
  }

  private WatchedLessonResponse toWatchedResponse(WatchedLessonRow row) {
    return new WatchedLessonResponse(
        row.lessonId(),
        row.courseId(),
        row.courseTitle(),
        row.lessonTitle(),
        row.lessonContent(),
        row.progressPercent());
  }

  private void validateCreate(LessonCreateRequest request) {
    if (request.courseId() <= 0) {
      throw new ApiException(ErrorCode.VALIDATION_ERROR, "CourseId is required");
    }
    validateTitle(request.title());
  }

  private void validateUpdate(LessonUpdateRequest request) {
    validateTitle(request.title());
  }

  private void validateTitle(String title) {
    if (title == null || title.isBlank()) {
      throw new ApiException(ErrorCode.VALIDATION_ERROR, "Title is required");
    }
  }
}
