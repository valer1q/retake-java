package org.example.taskmanager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerValidationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    public void shouldReturn400WhenTitleIsEmpty() throws Exception {
        TaskRequest request = new TaskRequest("", null, TaskStatus.TODO, null, null, null);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @Test
    public void shouldReturn400WhenDueDateInPast() throws Exception {
        TaskRequest request = new TaskRequest("Valid title", null, TaskStatus.TODO, LocalDateTime.now().minusDays(1), null, null);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("dueDate"));
    }

    @Test
    public void shouldReturn404WhenTaskNotFound() throws Exception {
        mockMvc.perform(get("/tasks/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn404WhenUserOfTaskNotFound() throws Exception {
        TaskRequest request = new TaskRequest("Valid title", null, TaskStatus.TODO, null, 999999L, null);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 999999"));
    }

    @Test
    public void shouldReturn400WhenIdIsNotUuid() throws Exception {
        mockMvc.perform(get("/tasks/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnStatusesFromDatabase() throws Exception {
        mockMvc.perform(get("/statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("TODO"))
                .andExpect(jsonPath("$[2].code").value("DONE"));
    }
}
