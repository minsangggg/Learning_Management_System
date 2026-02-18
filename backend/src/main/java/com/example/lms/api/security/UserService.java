package com.example.lms.api.security;

import com.example.lms.api.error.ApiException;
import com.example.lms.api.error.ErrorCode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository repository;
  private final PasswordEncoder encoder = new BCryptPasswordEncoder();

  public UserService(UserRepository repository) {
    this.repository = repository;
  }

  public AuthResponse register(SignupRequest request) {
    repository.findByEmail(request.email()).ifPresent(user -> {
      throw new ApiException(ErrorCode.VALIDATION_ERROR, "Email already registered");
    });
    String role = request.roleOrDefault();
    long userId =
        repository.insert(
            request.email(),
            encoder.encode(request.password()),
            role,
            request.name(),
            request.company());
    return new AuthResponse(userId, request.email(), role, request.name());
  }

  public AuthResponse authenticate(AuthRequest request) {
    UserEntity user =
        repository
            .findByEmail(request.email())
            .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "Invalid credentials"));
    if (!encoder.matches(request.password(), user.passwordHash())) {
      throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid credentials");
    }
    return new AuthResponse(user.id(), user.email(), user.role(), user.name());
  }

  public AuthResponse profile(long userId) {
    UserEntity user =
        repository
            .findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
    return new AuthResponse(user.id(), user.email(), user.role(), user.name());
  }
}
