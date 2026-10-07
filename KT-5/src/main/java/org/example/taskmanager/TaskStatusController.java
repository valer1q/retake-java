package org.example.taskmanager;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statuses")
public class TaskStatusController {

    private final TaskStatusRepository taskStatusRepository;

    public TaskStatusController(TaskStatusRepository taskStatusRepository) {
        this.taskStatusRepository = taskStatusRepository;
    }

    @GetMapping
    public List<TaskStatusInfo> getAllStatuses() {
        return taskStatusRepository.findAll();
    }
}
