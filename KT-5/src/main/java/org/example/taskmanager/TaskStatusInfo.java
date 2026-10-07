package org.example.taskmanager;

public record TaskStatusInfo(
        String code,
        String title,
        int sortOrder
) {}
