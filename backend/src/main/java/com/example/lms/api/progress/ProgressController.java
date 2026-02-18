package com.example.lms.api.progress;

import com.example.lms.api.enrollments.EnrollmentRepository;
import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import com.example.lms.api.progress.ProgressDtos.ProgressResponse;
import com.example.lms.api.progress.ProgressDtos.ProgressUpdateRequest;
import com.example.lms.api.security.AuthContext;
import com.example.lms.api.security.AuthUtil;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {
  private final ProgressRepository progressRepository;
  private final EnrollmentRepository enrollmentRepository;

  public ProgressController(ProgressRepository progressRepository, EnrollmentRepository enrollmentRepository) {
    this.progressRepository = progressRepository;
    this.enrollmentRepository = enrollmentRepository;
  }

  @GetMapping
  public List<ProgressResponse> list(
      @RequestParam("enrollmentId") long enrollmentId,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireLearner(ctx);
    enrollmentRepository.requireOwnedByUser(enrollmentId, ctx.userId());
    return progressRepository.findByEnrollmentId(enrollmentId).stream().map(this::toResponse).toList();
  }

  @PostMapping
  public ProgressResponse update(
      @RequestBody ProgressUpdateRequest request,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireLearner(ctx);
    validateRequest(request);
    enrollmentRepository.requireOwnedByUser(request.enrollmentId(), ctx.userId());
    boolean completed = request.progressPercent() >= 100.0;
    long id =
        progressRepository.upsert(
            request.enrollmentId(), request.lessonId(), request.progressPercent(), completed);
    return toResponse(progressRepository.findById(id));
  }

  private ProgressResponse toResponse(ProgressEntity entity) {
    return new ProgressResponse(
        entity.id(),
        entity.enrollmentId(),
        entity.lessonId(),
        entity.progressPercent(),
        entity.completedAt() == null ? null : entity.completedAt().toString());
  }

  private void validateRequest(ProgressUpdateRequest request) {
    if (request.enrollmentId() <= 0 || request.lessonId() <= 0) {
      throw new ApiException(ErrorCode.VALIDATION_ERROR, "EnrollmentId and LessonId are required");
    }
    if (request.progressPercent() < 0 || request.progressPercent() > 100) {
      throw new ApiException(ErrorCode.VALIDATION_ERROR, "Progress percent must be 0-100");
    }
  }
}
