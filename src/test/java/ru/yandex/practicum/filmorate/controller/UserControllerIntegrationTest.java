package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService.clearUsers();
    }

    @Test
    void getUsers_empty() throws Exception {
        var users = fetchUsers();
        assertThat(users).isEmpty();
    }

    @Test
    void addUser() throws Exception {
        var request = validUserDto();
        request.setId(null);

        var createResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var createdUser = objectMapper.readValue(createResponse, UserDto.class);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo(request.getEmail());
        assertThat(createdUser.getLogin()).isEqualTo(request.getLogin());
        assertThat(createdUser.getName()).isEqualTo(request.getName());
        assertThat(createdUser.getBirthday()).isEqualTo(request.getBirthday());

        var users = fetchUsers();
        assertThat(users).hasSize(1);
        assertThat(users.getFirst()).isEqualTo(createdUser);
    }

    @Test
    void updateUserExisting() throws Exception {
        var createRequest = validUserDto();
        createRequest.setId(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        var userToUpdate = fetchUsers().getFirst();
        userToUpdate.setEmail("updated@example.com");
        userToUpdate.setLogin("updatedLogin");
        userToUpdate.setName("Updated Name");
        userToUpdate.setBirthday(LocalDate.of(2001, 2, 3));

        var updateResponse = mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userToUpdate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var updatedUserFromResponse = objectMapper.readValue(updateResponse, UserDto.class);
        assertThat(updatedUserFromResponse).isEqualTo(userToUpdate);

        var users = fetchUsers();
        assertThat(users).hasSize(1);
        assertThat(users.getFirst()).isEqualTo(userToUpdate);
    }

    @Test
    void updateUserUnknown_returnsNotFound() throws Exception {
        var unknownUser = validUserDto();
        unknownUser.setId(9999L);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownUser)))
                .andExpect(status().isNotFound());

        assertThat(fetchUsers()).isEmpty();
    }

    @Test
    void addUser_withInvalidPayload() throws Exception {
        var invalidRequest = validUserDto();
        invalidRequest.setEmail("not-an-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        assertThat(fetchUsers()).isEmpty();
    }

    private UserDto validUserDto() {
        var dto = new UserDto();
        dto.setEmail("john@example.com");
        dto.setLogin("john_doe");
        dto.setName("John Doe");
        dto.setBirthday(LocalDate.of(2000, 1, 1));
        return dto;
    }

    private List<UserDto> fetchUsers() throws Exception {
        var response = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }
}
