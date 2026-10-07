package org.example.taskmanager;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record TaskRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
        String title,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Status is required")
        TaskStatus status,

        @Future(message = "Due date must be in the future")
        LocalDateTime dueDate,

        @Positive(message = "User id must be a positive number")
        Long userId,

        @Size(max = 10, message = "A task can have at most 10 tags")
        List<@NotBlank(message = "Tag must not be blank") @Size(max = 30, message = "Tag must not exceed 30 characters") String> tags
) {}
