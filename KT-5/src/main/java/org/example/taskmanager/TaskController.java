package org.example.taskmanager;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody TaskRequest request,
                                           @AuthenticationPrincipal AppUserDetails currentUser) {
        Task created = taskService.create(request, currentUser);
        return ResponseEntity
                .created(URI.create("/tasks/" + created.id()))
                .body(created);
    }

    @GetMapping
    public List<Task> getAllTasks(@AuthenticationPrincipal AppUserDetails currentUser) {
        return taskService.findAll(currentUser);
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        return taskService.findById(id, currentUser);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable UUID id, @Valid @RequestBody TaskRequest request,
                           @AuthenticationPrincipal AppUserDetails currentUser) {
        return taskService.update(id, request, currentUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        taskService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
