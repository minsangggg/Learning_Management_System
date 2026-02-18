package com.example.lms.api.security;

public record AuthResponse(long userId, String email, String role, String name) {}
