SELECT t.title,
       s.title    AS status,
       u.username AS owner,
       t.due_date
FROM tasks t
JOIN task_statuses s ON s.code = t.status_code
LEFT JOIN users u ON u.id = t.user_id
ORDER BY s.sort_order, t.created_at;

SELECT u.username,
       COUNT(t.id)                                              AS total,
       COUNT(t.id) FILTER (WHERE t.status_code = 'TODO')        AS todo,
       COUNT(t.id) FILTER (WHERE t.status_code = 'IN_PROGRESS') AS in_progress,
       COUNT(t.id) FILTER (WHERE t.status_code = 'DONE')        AS done
FROM users u
LEFT JOIN tasks t ON t.user_id = u.id
GROUP BY u.username
ORDER BY u.username;

SELECT g.name AS tag,
       t.title
FROM tags g
JOIN task_tags tt ON tt.tag_id = g.id
JOIN tasks t ON t.id = tt.task_id
ORDER BY g.name, t.title;

SELECT t.title,
       t.due_date,
       u.username AS owner
FROM tasks t
LEFT JOIN users u ON u.id = t.user_id
WHERE t.due_date < CURRENT_TIMESTAMP
  AND t.status_code <> 'DONE';

SELECT tc.table_name,
       tc.constraint_name,
       tc.constraint_type,
       kcu.column_name
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu
     ON kcu.constraint_name = tc.constraint_name
    AND kcu.table_schema = tc.table_schema
WHERE tc.table_schema = 'public'
  AND tc.constraint_type IN ('PRIMARY KEY', 'FOREIGN KEY', 'UNIQUE')
ORDER BY tc.table_name, tc.constraint_type, tc.constraint_name;
