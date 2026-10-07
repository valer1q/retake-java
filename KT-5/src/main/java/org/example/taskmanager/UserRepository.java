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

    private static final String SELECT_USERS = "SELECT id, username, email, role_code, created_at FROM users";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert userInsert;
    private final UserRowMapper userRowMapper = new UserRowMapper();
    private final UserAccountRowMapper userAccountRowMapper = new UserAccountRowMapper();

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingColumns("username", "email", "password_hash", "role_code", "created_at")
                .usingGeneratedKeyColumns("id");
    }

    public Long insert(String username, String email, String passwordHash, Role role) {
        Map<String, Object> values = new HashMap<>();
        values.put("username", username);
        values.put("email", email);
        values.put("password_hash", passwordHash);
        values.put("role_code", role.name());
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

    public UserAccount findAccountByUsername(String username) {
        List<UserAccount> accounts = jdbcTemplate.query(
                "SELECT id, username, password_hash, role_code, enabled FROM users WHERE LOWER(username) = LOWER(?)",
                userAccountRowMapper,
                username);
        if (accounts.isEmpty()) {
            return null;
        }
        return accounts.get(0);
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
