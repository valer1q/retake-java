package org.example.taskmanager;

public record UserAccount(
        Long id,
        String username,
        String passwordHash,
        Role role,
        boolean enabled
) {}
