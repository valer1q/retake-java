package org.example.taskmanager;

import org.junit.jupiter.api.BeforeEach;
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
    @Autowired UserRepository userRepository;

    AppUserDetails admin;
    AppUserDetails tester;

    @BeforeEach
    void createUsers() {
        userService.create(new CreateUserRequest("chief", "chief@mail.ru", "chief123", Role.ADMIN));
        userService.create(new CreateUserRequest("tester", "tester@mail.ru", "tester123", Role.USER));
        admin = new AppUserDetails(userRepository.findAccountByUsername("chief"));
        tester = new AppUserDetails(userRepository.findAccountByUsername("tester"));
    }

    @Test
    void shouldCreateTaskForCurrentUser() {
        TaskRequest request = new TaskRequest("Title", "Desc", TaskStatus.TODO, LocalDateTime.now().plusDays(1), null, null);
        Task created = service.create(request, tester);

        assertNotNull(created.id());
        assertNotNull(created.createdAt());
        assertEquals("Title", created.title());
        assertEquals(tester.getId(), created.userId());
    }

    @Test
    void shouldUpdateTaskAndKeepCreatedAt() {
        Task created = service.create(new TaskRequest("Old", null, TaskStatus.TODO, null, null, null), tester);

        Task updated = service.update(created.id(), new TaskRequest("New", "D", TaskStatus.DONE, null, null, null), tester);

        assertEquals("New", updated.title());
        assertEquals(TaskStatus.DONE, updated.status());
        assertEquals(created.createdAt(), updated.createdAt());
        assertEquals(tester.getId(), updated.userId());
    }

    @Test
    void shouldSaveNormalizedTags() {
        Task created = service.create(new TaskRequest("With tags", null, TaskStatus.IN_PROGRESS, null, null,
                List.of("Учёба", " срочно ", "учёба")), tester);

        assertEquals(List.of("срочно", "учёба"), created.tags());
    }

    @Test
    void shouldLetAdminAssignTaskToUser() {
        Task created = service.create(new TaskRequest("Assigned", null, TaskStatus.TODO, null, tester.getId(), null), admin);

        assertEquals(tester.getId(), created.userId());
        assertEquals(1, service.findAll(tester).size());
    }

    @Test
    void shouldDenyAccessToForeignTask() {
        Task adminTask = service.create(new TaskRequest("Admin only", null, TaskStatus.TODO, null, null, null), admin);

        assertThrows(TaskAccessDeniedException.class, () -> service.findById(adminTask.id(), tester));
        assertThrows(TaskAccessDeniedException.class, () -> service.delete(adminTask.id(), tester));
    }

    @Test
    void shouldThrowWhenDeleteMissingTask() {
        assertThrows(TaskNotFoundException.class, () -> service.delete(UUID.randomUUID(), tester));
    }

    @Test
    void shouldSetOwnerToNullWhenUserDeleted() {
        Task created = service.create(new TaskRequest("Orphan task", null, TaskStatus.TODO, null, null, null), tester);

        userService.delete(tester.getId(), admin);

        assertNull(service.findById(created.id(), admin).userId());
    }

    @Test
    void shouldRejectDuplicateUser() {
        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register(new RegisterRequest("TESTER", "other@mail.ru", "secret123")));
        assertThrows(UserAlreadyExistsException.class,
                () -> userService.register(new RegisterRequest("other", "TESTER@mail.ru", "secret123")));
    }
}
