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
    public Task create(TaskRequest request, AppUserDetails currentUser) {
        Long ownerId = resolveOwner(request.userId(), currentUser, currentUser.getId());
        List<String> tags = normalizeTags(request.tags());

        Task task = new Task(
                UUID.randomUUID(),
                request.title(),
                request.description(),
                request.status(),
                LocalDateTime.now().withNano(0),
                request.dueDate(),
                ownerId,
                tags
        );
        taskRepository.insert(task);
        taskRepository.replaceTags(task.id(), tags);
        return taskRepository.findById(task.id());
    }

    public Task findById(UUID id, AppUserDetails currentUser) {
        Task task = taskRepository.findById(id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }
        checkAccess(task, currentUser);
        return task;
    }

    public List<Task> findAll(AppUserDetails currentUser) {
        if (currentUser.isAdmin()) {
            return taskRepository.findAll();
        }
        return taskRepository.findByUserId(currentUser.getId());
    }

    public List<Task> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return taskRepository.findByUserId(userId);
    }

    @Transactional
    public Task update(UUID id, TaskRequest request, AppUserDetails currentUser) {
        Task existing = findById(id, currentUser);
        Long ownerId = resolveOwner(request.userId(), currentUser, existing.userId());

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
                ownerId,
                tags
        );
        taskRepository.update(updated);
        taskRepository.replaceTags(id, tags);
        return taskRepository.findById(id);
    }

    @Transactional
    public void delete(UUID id, AppUserDetails currentUser) {
        findById(id, currentUser);
        taskRepository.deleteById(id);
    }

    private void checkAccess(Task task, AppUserDetails currentUser) {
        if (currentUser.isAdmin()) {
            return;
        }
        if (task.userId() == null || !task.userId().equals(currentUser.getId())) {
            throw new TaskAccessDeniedException("You can access only your own tasks");
        }
    }

    private Long resolveOwner(Long requestedUserId, AppUserDetails currentUser, Long defaultOwnerId) {
        if (requestedUserId == null) {
            return defaultOwnerId;
        }
        if (!currentUser.isAdmin() && !requestedUserId.equals(currentUser.getId())) {
            throw new TaskAccessDeniedException("Only admin can assign tasks to other users");
        }
        if (!userRepository.existsById(requestedUserId)) {
            throw new UserNotFoundException(requestedUserId);
        }
        return requestedUserId;
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
