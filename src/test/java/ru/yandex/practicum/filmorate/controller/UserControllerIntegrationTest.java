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
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    @Autowired
    private UserStorage userStorage;

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
    void getFriends_emptyList() throws Exception {
        var user = createUser(validUserDto());

        var friends = fetchFriends(user.getId());

        assertThat(friends).isEmpty();
    }

    @Test
    void getUserById_existing() throws Exception {
        var createRequest = validUserDto();
        createRequest.setId(null);
        var createResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        var createdUser = objectMapper.readValue(createResponse, UserDto.class);

        var fetchedUser = fetchUser(createdUser.getId());

        assertThat(fetchedUser).isEqualTo(createdUser);
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

    @Test
    void addFriend() throws Exception {
        var firstUser = createUser(validUserDto());
        var secondUser = createUser(secondUserDto());

        mockMvc.perform(put("/users/{id}/friends/{friendId}", firstUser.getId(), secondUser.getId()))
                .andExpect(status().isOk());

        assertThat(userStorage.getUserById(firstUser.getId()).orElseThrow().getFriends()).contains(secondUser.getId());
        assertThat(userStorage.getUserById(secondUser.getId()).orElseThrow().getFriends()).contains(firstUser.getId());
    }

    @Test
    void addFriend_notFound() throws Exception {
        var existingUser = createUser(validUserDto());
        var unknownId = 9999L;

        mockMvc.perform(put("/users/{id}/friends/{friendId}", existingUser.getId(), unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    void addFriend_sameUserId_returnsBadRequest() throws Exception {
        var user = createUser(validUserDto());

        mockMvc.perform(put("/users/{id}/friends/{friendId}", user.getId(), user.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteFriend() throws Exception {
        var firstUser = createUser(validUserDto());
        var secondUser = createUser(secondUserDto());

        mockMvc.perform(put("/users/{id}/friends/{friendId}", firstUser.getId(), secondUser.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", firstUser.getId(), secondUser.getId()))
                .andExpect(status().isOk());

        assertThat(userStorage.getUserById(firstUser.getId()).orElseThrow().getFriends())
                .doesNotContain(secondUser.getId());
        assertThat(userStorage.getUserById(secondUser.getId()).orElseThrow().getFriends())
                .doesNotContain(firstUser.getId());
    }

    @Test
    void deleteFriend_sameUserId() throws Exception {
        var user = createUser(validUserDto());

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", user.getId(), user.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFriends() throws Exception {
        var user = createUser(validUserDto());
        var firstFriend = createUser(secondUserDto());
        var secondFriend = createUser(thirdUserDto());

        mockMvc.perform(put("/users/{id}/friends/{friendId}", user.getId(), firstFriend.getId()))
                .andExpect(status().isOk());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", user.getId(), secondFriend.getId()))
                .andExpect(status().isOk());

        var friends = fetchFriends(user.getId());

        assertThat(friends).hasSize(2);
        assertThat(friends).contains(firstFriend, secondFriend);
    }

    @Test
    void getFriends_notFound() throws Exception {
        var unknownId = 9999L;

        mockMvc.perform(get("/users/{id}/friends", unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCommonFriends() throws Exception {
        var firstUser = createUser(validUserDto());
        var secondUser = createUser(validUserDto());
        var firstFriend = createUser(secondUserDto());
        var secondFriend = createUser(thirdUserDto());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", firstUser.getId(), firstFriend.getId()))
                .andExpect(status().isOk());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", firstUser.getId(), secondFriend.getId()))
                .andExpect(status().isOk());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", secondUser.getId(), secondFriend.getId()))
                .andExpect(status().isOk());

        var friends = getCommonFriends(firstUser.getId(), secondUser.getId());

        assertThat(friends).hasSize(1);
        assertThat(friends).contains(secondFriend);
    }

    @Test
    void getCommonFriends_noCommon() throws Exception {
        var firstUser = createUser(validUserDto());
        var secondUser = createUser(validUserDto());
        var firstFriend = createUser(secondUserDto());
        var secondFriend = createUser(thirdUserDto());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", firstUser.getId(), firstFriend.getId()))
                .andExpect(status().isOk());
        mockMvc.perform(put("/users/{id}/friends/{friendId}", firstUser.getId(), secondFriend.getId()))
                .andExpect(status().isOk());

        var friends = getCommonFriends(firstUser.getId(), secondUser.getId());

        assertThat(friends).isEmpty();
    }

    @Test
    void getCommonFriends_sameUserId() throws Exception {
        var user = createUser(validUserDto());

        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", user.getId(), user.getId()))
                .andExpect(status().isBadRequest());
    }

    private UserDto validUserDto() {
        var dto = new UserDto();
        dto.setEmail("john@example.com");
        dto.setLogin("john_doe");
        dto.setName("John Doe");
        dto.setBirthday(LocalDate.of(2000, 1, 1));
        return dto;
    }

    private UserDto secondUserDto() {
        var dto = new UserDto();
        dto.setEmail("alice@example.com");
        dto.setLogin("alice");
        dto.setName("Alice");
        dto.setBirthday(LocalDate.of(1999, 5, 20));
        return dto;
    }

    private UserDto thirdUserDto() {
        var dto = new UserDto();
        dto.setEmail("bob@example.com");
        dto.setLogin("bob");
        dto.setName("Bob");
        dto.setBirthday(LocalDate.of(1998, 8, 15));
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

    private UserDto fetchUser(Long id) throws Exception {
        var response = mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, UserDto.class);
    }

    private UserDto createUser(UserDto request) throws Exception {
        request.setId(null);
        var createResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(createResponse, UserDto.class);
    }

    private List<UserDto> fetchFriends(Long id) throws Exception {
        var response = mockMvc.perform(get("/users/{id}/friends", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }

    private List<UserDto> getCommonFriends(Long id, Long otherId) throws Exception {
        var response = mockMvc.perform(get("/users/{id}/friends/common/{otherId}", id, otherId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }
}
