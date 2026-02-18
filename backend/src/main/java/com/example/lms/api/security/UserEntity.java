package com.example.lms.api.security;

import java.time.Instant;

public record UserEntity(
    long id,
    String email,
    String passwordHash,
    String role,
    String name,
    String company,
    Instant createdAt) {}
