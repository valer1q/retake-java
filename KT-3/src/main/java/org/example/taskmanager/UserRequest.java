package org.example.taskmanager;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(regexp = "[A-Za-z0-9_]*", message = "Username may contain only latin letters, digits and _")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email has invalid format")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email
) {}
