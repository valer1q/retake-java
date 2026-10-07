package org.example.taskmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserService userService;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void createUsers() {
        userService.create(new CreateUserRequest("boss", "boss@mail.ru", "boss123", Role.ADMIN));
        userService.create(new CreateUserRequest("alice", "alice@mail.ru", "alice123", Role.USER));
        userService.create(new CreateUserRequest("bob", "bob@mail.ru", "bob123", Role.USER));
    }

    @Test
    void shouldReturn401WithoutCredentials() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"taskmanager\""))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldNotAskBrowserForPasswordFromJavaScript() throws Exception {
        mockMvc.perform(get("/tasks").header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void shouldReturn401WithWrongPassword() throws Exception {
        mockMvc.perform(get("/tasks").with(httpBasic("alice", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void shouldRegisterUserAndStorePasswordHash() throws Exception {
        String body = "{\"username\":\"newbie\",\"email\":\"newbie@mail.ru\",\"password\":\"secret123\"}";
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newbie"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        UserAccount account = userRepository.findAccountByUsername("newbie");
        assertNotEquals("secret123", account.passwordHash());
        assertTrue(account.passwordHash().startsWith("$2a$"));

        mockMvc.perform(get("/auth/me").with(httpBasic("newbie", "secret123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newbie"));
    }

    @Test
    void shouldReturn409WhenUsernameTaken() throws Exception {
        String body = "{\"username\":\"ALICE\",\"email\":\"other@mail.ru\",\"password\":\"secret123\"}";
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400WhenRegisterPasswordTooShort() throws Exception {
        String body = "{\"username\":\"shorty\",\"email\":\"shorty@mail.ru\",\"password\":\"123\"}";
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("password"));
    }

    @Test
    void shouldAllowStatusesWithoutLogin() throws Exception {
        mockMvc.perform(get("/statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("TODO"));
    }

    @Test
    void shouldForbidUsersEndpointForRegularUser() throws Exception {
        mockMvc.perform(get("/users").with(httpBasic("alice", "alice123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shouldAllowUsersEndpointForAdmin() throws Exception {
        mockMvc.perform(get("/users").with(httpBasic("boss", "boss123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldNotAllowAdminToDeleteHimself() throws Exception {
        Long bossId = userRepository.findAccountByUsername("boss").id();
        mockMvc.perform(delete("/users/" + bossId).with(httpBasic("boss", "boss123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateTaskForCurrentUserAndHideItFromOthers() throws Exception {
        Long aliceId = userRepository.findAccountByUsername("alice").id();
        TaskRequest request = new TaskRequest("Alice task", null, TaskStatus.TODO, null, null, null);

        String response = mockMvc.perform(post("/tasks")
                        .with(httpBasic("alice", "alice123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(aliceId))
                .andReturn().getResponse().getContentAsString();
        String taskId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/tasks/" + taskId).with(httpBasic("bob", "bob123")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/tasks").with(httpBasic("bob", "bob123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/tasks/" + taskId).with(httpBasic("boss", "boss123")))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/tasks/" + taskId).with(httpBasic("bob", "bob123")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/tasks/" + taskId).with(httpBasic("alice", "alice123")))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldForbidUserToAssignTaskToSomeoneElse() throws Exception {
        Long bobId = userRepository.findAccountByUsername("bob").id();
        TaskRequest request = new TaskRequest("For Bob", null, TaskStatus.TODO, null, bobId, null);

        mockMvc.perform(post("/tasks")
                        .with(httpBasic("alice", "alice123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn400WhenTitleIsEmpty() throws Exception {
        TaskRequest request = new TaskRequest("", null, TaskStatus.TODO, null, null, null);

        mockMvc.perform(post("/tasks")
                        .with(httpBasic("alice", "alice123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @Test
    void shouldReturn400WhenDueDateInPast() throws Exception {
        TaskRequest request = new TaskRequest("Valid title", null, TaskStatus.TODO, LocalDateTime.now().minusDays(1), null, null);

        mockMvc.perform(post("/tasks")
                        .with(httpBasic("alice", "alice123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("dueDate"));
    }

    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        mockMvc.perform(get("/tasks/" + UUID.randomUUID()).with(httpBasic("alice", "alice123")))
                .andExpect(status().isNotFound());
    }
}
