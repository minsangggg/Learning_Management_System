package com.example.lms.api.security;

public record SignupRequest(String email, String password, String role, String name, String company) {
  public String roleOrDefault() {
    return role == null || role.isBlank() ? "LEARNER" : role.toUpperCase();
  }
}
