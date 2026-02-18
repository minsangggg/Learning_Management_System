package com.example.lms.api.security;

import java.util.Optional;
import com.example.lms.api.security.AuthContext;
import com.example.lms.api.security.AuthUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/signup")
  public AuthResponse signup(@RequestBody SignupRequest request) {
    return userService.register(request);
  }

  @PostMapping("/auth/login")
  public AuthResponse login(@RequestBody AuthRequest request) {
    return userService.authenticate(request);
  }

  @GetMapping("/users/me")
  public AuthResponse me(
      @RequestHeader("X-User-Id") Optional<String> userIdHeader,
      @RequestHeader("X-Role") Optional<String> roleHeader) {
    AuthContext ctx = AuthUtil.requireAuth(userIdHeader, roleHeader);
    return userService.profile(ctx.userId());
  }
}
