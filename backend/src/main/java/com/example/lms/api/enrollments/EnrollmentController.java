package com.example.lms.api.enrollments;

import com.example.lms.api.enrollments.EnrollmentDtos.EnrollRequest;
import com.example.lms.api.enrollments.EnrollmentDtos.EnrollmentResponse;
import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import com.example.lms.api.security.AuthContext;
import com.example.lms.api.security.AuthUtil;
import java.util.Optional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enroll")
public class EnrollmentController {
  private final EnrollmentRepository repository;

  public EnrollmentController(EnrollmentRepository repository) {
    this.repository = repository;
  }

  @PostMapping
  public EnrollmentResponse enroll(
      @RequestBody EnrollRequest request,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    AuthUtil.requireLearner(ctx);
    if (request.courseId() <= 0) {
      throw new ApiException(ErrorCode.VALIDATION_ERROR, "CourseId is required");
    }
    long id = repository.enroll(ctx.userId(), request.courseId());
    return toResponse(repository.findById(id));
  }

  private EnrollmentResponse toResponse(EnrollmentEntity entity) {
    return new EnrollmentResponse(
        entity.id(),
        entity.userId(),
        entity.courseId(),
        entity.status(),
        entity.enrolledAt().toString());
  }
}
