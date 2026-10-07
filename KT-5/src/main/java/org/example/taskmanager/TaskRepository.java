package org.example.taskmanager;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class TaskRepository {

    private static final String SELECT_TASKS =
            "SELECT id, title, description, status_code, created_at, due_date, user_id FROM tasks";

    private final JdbcTemplate jdbcTemplate;
    private final TaskRowMapper taskRowMapper = new TaskRowMapper();

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(Task task) {
        jdbcTemplate.update(
                "INSERT INTO tasks (id, title, description, status_code, created_at, due_date, user_id) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                task.id(),
                task.title(),
                task.description(),
                task.status().name(),
                task.createdAt(),
                task.dueDate(),
                task.userId()
        );
    }

    public void update(Task task) {
        jdbcTemplate.update(
                "UPDATE tasks SET title = ?, description = ?, status_code = ?, due_date = ?, user_id = ? WHERE id = ?",
                task.title(),
                task.description(),
                task.status().name(),
                task.dueDate(),
                task.userId(),
                task.id()
        );
    }

    public int deleteById(UUID id) {
        return jdbcTemplate.update("DELETE FROM tasks WHERE id = ?", id);
    }

    public Task findById(UUID id) {
        List<Task> tasks = jdbcTemplate.query(SELECT_TASKS + " WHERE id = ?", taskRowMapper, id);
        if (tasks.isEmpty()) {
            return null;
        }
        loadTags(tasks);
        return tasks.get(0);
    }

    public List<Task> findAll() {
        List<Task> tasks = jdbcTemplate.query(SELECT_TASKS + " ORDER BY created_at DESC, id", taskRowMapper);
        loadTags(tasks);
        return tasks;
    }

    public List<Task> findByUserId(Long userId) {
        List<Task> tasks = jdbcTemplate.query(
                SELECT_TASKS + " WHERE user_id = ? ORDER BY created_at DESC, id", taskRowMapper, userId);
        loadTags(tasks);
        return tasks;
    }

    public void replaceTags(UUID taskId, List<String> tagNames) {
        jdbcTemplate.update("DELETE FROM task_tags WHERE task_id = ?", taskId);
        for (String name : tagNames) {
            Long tagId = findOrCreateTag(name);
            jdbcTemplate.update("INSERT INTO task_tags (task_id, tag_id) VALUES (?, ?)", taskId, tagId);
        }
    }

    private Long findOrCreateTag(String name) {
        List<Long> ids = jdbcTemplate.queryForList("SELECT id FROM tags WHERE name = ?", Long.class, name);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }
        jdbcTemplate.update("INSERT INTO tags (name) VALUES (?)", name);
        return jdbcTemplate.queryForObject("SELECT id FROM tags WHERE name = ?", Long.class, name);
    }

    private void loadTags(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return;
        }

        Map<UUID, Task> tasksById = new HashMap<>();
        StringBuilder placeholders = new StringBuilder();
        Object[] ids = new Object[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            tasksById.put(task.id(), task);
            ids[i] = task.id();
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }

        String sql = "SELECT tt.task_id, g.name FROM task_tags tt "
                + "JOIN tags g ON g.id = tt.tag_id "
                + "WHERE tt.task_id IN (" + placeholders + ") "
                + "ORDER BY g.name";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, ids);
        for (Map<String, Object> row : rows) {
            Task task = tasksById.get((UUID) row.get("task_id"));
            if (task != null) {
                task.tags().add((String) row.get("name"));
            }
        }
    }
}
