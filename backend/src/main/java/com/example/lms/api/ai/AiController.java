package com.example.lms.api.ai;

import com.example.lms.api.ai.AiDtos.AiRequest;
import com.example.lms.api.ai.AiDtos.AiRequestLogResponse;
import com.example.lms.api.ai.AiDtos.AiResponse;
import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
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
@RequestMapping("/api/ai")
public class AiController {
  private final AiService service;
  private final AiRepository repository;

  public AiController(AiService service, AiRepository repository) {
    this.service = service;
    this.repository = repository;
  }

  @GetMapping("/requests")
  public List<AiRequestLogResponse> requests(
      @RequestParam(name = "userId", required = false) Long userId,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    if ("ADMIN".equalsIgnoreCase(ctx.role())) {
      return repository.findRequests(userId);
    }
    if ("LEARNER".equalsIgnoreCase(ctx.role())) {
      return repository.findRequests(ctx.userId());
    }
    throw new ApiException(ErrorCode.FORBIDDEN, "Unsupported role");
  }

  @PostMapping("/summary")
  public AiResponse summary(
      @RequestBody AiRequest request,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    validate(request);
    AiService.AiResult result = service.generateSummary(ctx.userId(), request.text());
    return new AiResponse(result.output(), result.status(), result.latencyMs());
  }

  @PostMapping("/quiz")
  public AiResponse quiz(
      @RequestBody AiRequest request,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    validate(request);
    AiService.AiResult result = service.generateQuiz(ctx.userId(), request.text());
    return new AiResponse(result.output(), result.status(), result.latencyMs());
  }

  private void validate(AiRequest request) {
    if (request == null || request.text() == null || request.text().isBlank()) {
      throw new ApiException(ErrorCode.VALIDATION_ERROR, "Text is required");
    }
  }
}
