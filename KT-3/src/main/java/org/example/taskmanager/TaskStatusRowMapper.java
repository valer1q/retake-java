package org.example.taskmanager;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskStatusRowMapper implements RowMapper<TaskStatusInfo> {

    @Override
    public TaskStatusInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new TaskStatusInfo(
                rs.getString("code"),
                rs.getString("title"),
                rs.getInt("sort_order")
        );
    }
}
