CREATE USER taskmanager_user WITH PASSWORD 'taskmanager_pass';

CREATE DATABASE taskmanager
    WITH OWNER = taskmanager_user
    ENCODING = 'UTF8'
    TEMPLATE = template0;

COMMENT ON DATABASE taskmanager IS 'База данных веб-приложения Task Manager';
