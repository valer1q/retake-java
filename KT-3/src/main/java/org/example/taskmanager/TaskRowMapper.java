package org.example.taskmanager;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class TaskRowMapper implements RowMapper<Task> {

    @Override
    public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Task(
                rs.getObject("id", UUID.class),
                rs.getString("title"),
                rs.getString("description"),
                TaskStatus.valueOf(rs.getString("status_code")),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("due_date", LocalDateTime.class),
                rs.getObject("user_id", Long.class),
                new ArrayList<>()
        );
    }
}
