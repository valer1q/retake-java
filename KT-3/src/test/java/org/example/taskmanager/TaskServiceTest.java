package org.example.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class TaskServiceTest {

    @Autowired TaskService service;
    @Autowired UserService userService;

    @Test
    void shouldCreateTask() {
        TaskRequest request = new TaskRequest("Title", "Desc", TaskStatus.TODO, LocalDateTime.now().plusDays(1), null, null);
        Task created = service.create(request);

        assertNotNull(created.id());
        assertNotNull(created.createdAt());
        assertEquals("Title", created.title());
        assertEquals(TaskStatus.TODO, created.status());
        assertEquals(created, service.findById(created.id()));
    }

    @Test
    void shouldUpdateTask() {
        Task created = service.create(new TaskRequest("Old", null, TaskStatus.TODO, null, null, null));
        UUID id = created.id();

        Task updated = service.update(id, new TaskRequest("New", "D", TaskStatus.DONE, LocalDateTime.now().plusDays(2), null, null));

        assertEquals(id, updated.id());
        assertEquals("New", updated.title());
        assertEquals(TaskStatus.DONE, updated.status());
        assertEquals(created.createdAt(), updated.createdAt());
    }

    @Test
    void shouldThrowWhenDeleteMissingTask() {
        assertThrows(TaskNotFoundException.class, () -> service.delete(UUID.randomUUID()));
    }

    @Test
    void shouldSaveUserAndTags() {
        User user = userService.create(new UserRequest("tester", "tester@mail.ru"));
        Task created = service.create(new TaskRequest("With tags", null, TaskStatus.IN_PROGRESS, null, user.id(),
                List.of("Учёба", " срочно ", "учёба")));

        assertEquals(user.id(), created.userId());
        assertEquals(List.of("срочно", "учёба"), created.tags());
        assertEquals(1, service.findByUserId(user.id()).size());
    }

    @Test
    void shouldKeepUserAndTagsWhenUpdateDoesNotSendThem() {
        User user = userService.create(new UserRequest("keeper", "keeper@mail.ru"));
        Task created = service.create(new TaskRequest("Keep links", null, TaskStatus.TODO, null, user.id(), List.of("дом")));

        Task updated = service.update(created.id(), new TaskRequest("Keep links 2", null, TaskStatus.DONE, null, null, null));

        assertEquals(user.id(), updated.userId());
        assertEquals(List.of("дом"), updated.tags());
    }

    @Test
    void shouldSetUserToNullWhenUserDeleted() {
        User user = userService.create(new UserRequest("leaving", "leaving@mail.ru"));
        Task created = service.create(new TaskRequest("Orphan task", null, TaskStatus.TODO, null, user.id(), null));

        userService.delete(user.id());

        assertNull(service.findById(created.id()).userId());
    }

    @Test
    void shouldRejectDuplicateUser() {
        userService.create(new UserRequest("twin", "twin@mail.ru"));
        assertThrows(UserAlreadyExistsException.class, () -> userService.create(new UserRequest("TWIN", "other@mail.ru")));
        assertThrows(UserAlreadyExistsException.class, () -> userService.create(new UserRequest("other", "TWIN@mail.ru")));
    }
}
