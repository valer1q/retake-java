package org.example.taskmanager;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TaskStatusRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TaskStatusRowMapper taskStatusRowMapper = new TaskStatusRowMapper();

    public TaskStatusRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TaskStatusInfo> findAll() {
        return jdbcTemplate.query(
                "SELECT code, title, sort_order FROM task_statuses ORDER BY sort_order", taskStatusRowMapper);
    }
}
