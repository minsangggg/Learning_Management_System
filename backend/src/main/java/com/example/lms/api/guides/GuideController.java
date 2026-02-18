package com.example.lms.api.guides;

import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import com.example.lms.api.guides.GuideDtos.GuideResponse;
import com.example.lms.api.security.AuthContext;
import com.example.lms.api.security.AuthUtil;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guides")
public class GuideController {
  private final GuideRepository repository;

  public GuideController(GuideRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<GuideResponse> list(
      @RequestParam(name = "userId", required = false) Long userId,
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    if ("ADMIN".equalsIgnoreCase(ctx.role())) {
      return repository.findGuides(userId);
    }
    if ("LEARNER".equalsIgnoreCase(ctx.role())) {
      return repository.findGuides(ctx.userId());
    }
    throw new ApiException(ErrorCode.FORBIDDEN, "Unsupported role");
  }
}
