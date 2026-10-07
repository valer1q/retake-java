package org.example.taskmanager;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserRepository {

    private static final String SELECT_USERS = "SELECT id, username, email, created_at FROM users";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert userInsert;
    private final UserRowMapper userRowMapper = new UserRowMapper();

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingColumns("username", "email", "created_at")
                .usingGeneratedKeyColumns("id");
    }

    public Long insert(String username, String email) {
        Map<String, Object> values = new HashMap<>();
        values.put("username", username);
        values.put("email", email);
        values.put("created_at", LocalDateTime.now().withNano(0));
        Number id = userInsert.executeAndReturnKey(values);
        return id.longValue();
    }

    public User findById(Long id) {
        List<User> users = jdbcTemplate.query(SELECT_USERS + " WHERE id = ?", userRowMapper, id);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    public List<User> findAll() {
        return jdbcTemplate.query(SELECT_USERS + " ORDER BY id", userRowMapper);
    }

    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)", Integer.class, username);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)", Integer.class, email);
        return count != null && count > 0;
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }
}
