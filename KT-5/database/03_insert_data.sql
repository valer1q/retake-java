INSERT INTO users (username, email, password_hash, role_code, created_at) VALUES
    ('admin', 'admin@mail.ru', '$2a$10$LG.9fdpjjcoPi6jO1.o8puvCEEUuRvXrO813nUejmnN7fa3pVcqSC', 'ADMIN', '2026-09-10 08:00:00'),
    ('alex', 'alex@mail.ru', '$2a$10$9fxTOnQYrUZY6iW0ApxfhO0zovmU1cXWSHbPhZ9oaEbZuw9qidqbW', 'USER', '2026-09-15 09:00:00'),
    ('maria', 'maria@mail.ru', '$2a$10$GS/UfXAI9vIg58HRif3MNun8cRDFvNFtuQrfIt6CnY4rkrDwMuIVe', 'USER', '2026-09-16 11:20:00'),
    ('ivan', 'ivan@mail.ru', '$2a$10$UEEZN22LRfqhul9lCvi8K.YwQjnBnlcn2P2SLOiM262JEQZx7tm1e', 'USER', '2026-09-18 17:45:00');

INSERT INTO tags (name) VALUES
    ('учёба'),
    ('работа'),
    ('срочно'),
    ('дом');

INSERT INTO tasks (id, title, description, status_code, created_at, due_date, user_id) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Изучить SQL', 'Повторить JOIN и внешние ключи', 'IN_PROGRESS',
        '2026-10-01 10:00:00', '2026-12-01 18:00:00', (SELECT id FROM users WHERE username = 'alex')),
    ('22222222-2222-2222-2222-222222222222', 'Сдать КТ-5', 'Spring Security и базовая аутентификация', 'TODO',
        '2026-10-02 12:30:00', '2026-11-15 23:59:00', (SELECT id FROM users WHERE username = 'alex')),
    ('33333333-3333-3333-3333-333333333333', 'Подготовить отчёт', NULL, 'DONE',
        '2026-09-20 09:00:00', '2026-09-30 18:00:00', (SELECT id FROM users WHERE username = 'maria')),
    ('44444444-4444-4444-4444-444444444444', 'Купить продукты', 'Хлеб, молоко, сыр', 'TODO',
        '2026-10-05 08:15:00', NULL, (SELECT id FROM users WHERE username = 'maria')),
    ('55555555-5555-5555-5555-555555555555', 'Задача без владельца', 'Пример задачи с user_id = NULL', 'TODO',
        '2026-10-06 14:00:00', NULL, NULL);

INSERT INTO task_tags (task_id, tag_id) VALUES
    ('11111111-1111-1111-1111-111111111111', (SELECT id FROM tags WHERE name = 'учёба')),
    ('22222222-2222-2222-2222-222222222222', (SELECT id FROM tags WHERE name = 'учёба')),
    ('22222222-2222-2222-2222-222222222222', (SELECT id FROM tags WHERE name = 'срочно')),
    ('33333333-3333-3333-3333-333333333333', (SELECT id FROM tags WHERE name = 'работа')),
    ('44444444-4444-4444-4444-444444444444', (SELECT id FROM tags WHERE name = 'дом'));
