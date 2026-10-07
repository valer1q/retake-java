DROP TABLE IF EXISTS task_tags CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS tags CASCADE;
DROP TABLE IF EXISTS task_statuses CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

CREATE TABLE roles (
    code   VARCHAR(20) NOT NULL,
    title  VARCHAR(50) NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (code)
);

CREATE TABLE users (
    id             BIGINT GENERATED ALWAYS AS IDENTITY,
    username       VARCHAR(50)  NOT NULL,
    email          VARCHAR(100) NOT NULL,
    password_hash  VARCHAR(100) NOT NULL,
    role_code      VARCHAR(20)  NOT NULL DEFAULT 'USER',
    enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_role FOREIGN KEY (role_code)
        REFERENCES roles (code)
);

CREATE TABLE task_statuses (
    code        VARCHAR(20) NOT NULL,
    title       VARCHAR(50) NOT NULL,
    sort_order  INT         NOT NULL,
    CONSTRAINT pk_task_statuses PRIMARY KEY (code),
    CONSTRAINT uq_task_statuses_sort_order UNIQUE (sort_order)
);

CREATE TABLE tasks (
    id           UUID         NOT NULL,
    user_id      BIGINT,
    status_code  VARCHAR(20)  NOT NULL,
    title        VARCHAR(100) NOT NULL,
    description  VARCHAR(500),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date     TIMESTAMP,
    CONSTRAINT pk_tasks PRIMARY KEY (id),
    CONSTRAINT fk_tasks_status FOREIGN KEY (status_code)
        REFERENCES task_statuses (code),
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT chk_tasks_title_length CHECK (CHAR_LENGTH(title) BETWEEN 3 AND 100)
);

CREATE TABLE tags (
    id    BIGINT GENERATED ALWAYS AS IDENTITY,
    name  VARCHAR(30) NOT NULL,
    CONSTRAINT pk_tags PRIMARY KEY (id),
    CONSTRAINT uq_tags_name UNIQUE (name)
);

CREATE TABLE task_tags (
    task_id  UUID   NOT NULL,
    tag_id   BIGINT NOT NULL,
    CONSTRAINT pk_task_tags PRIMARY KEY (task_id, tag_id),
    CONSTRAINT fk_task_tags_task FOREIGN KEY (task_id)
        REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_tags_tag FOREIGN KEY (tag_id)
        REFERENCES tags (id) ON DELETE CASCADE
);

CREATE INDEX idx_users_role_code ON users (role_code);
CREATE INDEX idx_tasks_user_id ON tasks (user_id);
CREATE INDEX idx_tasks_status_code ON tasks (status_code);
CREATE INDEX idx_tasks_due_date ON tasks (due_date);
CREATE INDEX idx_task_tags_tag_id ON task_tags (tag_id);

COMMENT ON TABLE roles IS 'Справочник ролей пользователей для Spring Security';
COMMENT ON TABLE users IS 'Пользователи приложения: логин, хеш пароля (BCrypt), роль';
COMMENT ON COLUMN users.password_hash IS 'Хеш пароля BCrypt, сам пароль не хранится';
COMMENT ON TABLE task_statuses IS 'Справочник статусов задач';
COMMENT ON TABLE tasks IS 'Задачи';
COMMENT ON TABLE tags IS 'Теги (метки) задач';
COMMENT ON TABLE task_tags IS 'Связь многие-ко-многим между задачами и тегами';

INSERT INTO roles (code, title) VALUES
    ('USER', 'Пользователь'),
    ('ADMIN', 'Администратор');

INSERT INTO task_statuses (code, title, sort_order) VALUES
    ('TODO', 'К выполнению', 1),
    ('IN_PROGRESS', 'В работе', 2),
    ('DONE', 'Выполнена', 3);
