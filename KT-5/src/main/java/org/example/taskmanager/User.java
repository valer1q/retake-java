package org.example.taskmanager;

import java.time.LocalDateTime;

public record User(
        Long id,
        String username,
        String email,
        Role role,
        LocalDateTime createdAt
) {}
