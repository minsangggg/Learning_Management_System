package com.example.lms.api.security;

import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import java.util.Optional;

public final class AuthUtil {
  private AuthUtil() {}

  public static AuthContext requireAuth(Optional<String> userIdHeader, Optional<String> roleHeader) {
    String userIdValue = userIdHeader.orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Missing X-User-Id"));
    String role = roleHeader.orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Missing X-Role"));
    long userId;
    try {
      userId = Long.parseLong(userIdValue);
    } catch (NumberFormatException ex) {
      throw new ApiException(ErrorCode.VALIDATION_ERROR, "Invalid X-User-Id");
    }
    return new AuthContext(userId, role);
  }

  public static void requireAdmin(AuthContext ctx) {
    if (!"ADMIN".equalsIgnoreCase(ctx.role())) {
      throw new ApiException(ErrorCode.FORBIDDEN, "Admin role required");
    }
  }

  public static void requireLearner(AuthContext ctx) {
    if (!"LEARNER".equalsIgnoreCase(ctx.role())) {
      throw new ApiException(ErrorCode.FORBIDDEN, "Learner role required");
    }
  }
}
