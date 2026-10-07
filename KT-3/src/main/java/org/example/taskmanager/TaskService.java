package org.example.taskmanager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Task create(TaskRequest request) {
        checkUserExists(request.userId());
        List<String> tags = normalizeTags(request.tags());

        Task task = new Task(
                UUID.randomUUID(),
                request.title(),
                request.description(),
                request.status(),
                LocalDateTime.now().withNano(0),
                request.dueDate(),
                request.userId(),
                tags
        );
        taskRepository.insert(task);
        taskRepository.replaceTags(task.id(), tags);
        return findById(task.id());
    }

    public Task findById(UUID id) {
        Task task = taskRepository.findById(id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }
        return task;
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public List<Task> findByUserId(Long userId) {
        checkUserExists(userId);
        return taskRepository.findByUserId(userId);
    }

    @Transactional
    public Task update(UUID id, TaskRequest request) {
        Task existing = findById(id);

        Long userId = existing.userId();
        if (request.userId() != null) {
            checkUserExists(request.userId());
            userId = request.userId();
        }

        List<String> tags = existing.tags();
        if (request.tags() != null) {
            tags = normalizeTags(request.tags());
        }

        Task updated = new Task(
                id,
                request.title(),
                request.description(),
                request.status(),
                existing.createdAt(),
                request.dueDate(),
                userId,
                tags
        );
        taskRepository.update(updated);
        taskRepository.replaceTags(id, tags);
        return findById(id);
    }

    @Transactional
    public void delete(UUID id) {
        int deleted = taskRepository.deleteById(id);
        if (deleted == 0) {
            throw new TaskNotFoundException(id);
        }
    }

    private void checkUserExists(Long userId) {
        if (userId != null && !userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
    }

    private List<String> normalizeTags(List<String> rawTags) {
        List<String> result = new ArrayList<>();
        if (rawTags == null) {
            return result;
        }
        for (String rawTag : rawTags) {
            if (rawTag == null) {
                continue;
            }
            String tag = rawTag.trim().toLowerCase();
            if (!tag.isEmpty() && !result.contains(tag)) {
                result.add(tag);
            }
        }
        return result;
    }
}
