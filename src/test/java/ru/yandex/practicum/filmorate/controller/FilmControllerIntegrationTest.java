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
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

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
class FilmControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilmService filmService;

    @Autowired
    private UserService userService;

    @Autowired
    private FilmStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmService.clearFilms();
        userService.clearUsers();
    }

    @Test
    void getFilms_emptyList() throws Exception {
        var films = fetchFilms();
        assertThat(films).isEmpty();
    }

    @Test
    void addFilm() throws Exception {
        var request = validFilmDto();
        request.setId(null);

        var createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var createdFilm = objectMapper.readValue(createResponse, FilmDto.class);
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo(request.getName());
        assertThat(createdFilm.getDescription()).isEqualTo(request.getDescription());
        assertThat(createdFilm.getReleaseDate()).isEqualTo(request.getReleaseDate());
        assertThat(createdFilm.getDuration()).isEqualTo(request.getDuration());

        var films = fetchFilms();
        assertThat(films).hasSize(1);
        assertThat(films.getFirst()).isEqualTo(createdFilm);
    }

    @Test
    void updateFilm() throws Exception {
        var createRequest = validFilmDto();
        createRequest.setId(null);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        var filmToUpdate = fetchFilms().getFirst();
        filmToUpdate.setName("Updated Name");
        filmToUpdate.setDescription("Updated description");
        filmToUpdate.setReleaseDate(LocalDate.of(2024, 2, 1));
        filmToUpdate.setDuration(95);

        var updateResponse = mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmToUpdate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var updatedFilmFromResponse = objectMapper.readValue(updateResponse, FilmDto.class);
        assertThat(updatedFilmFromResponse).isEqualTo(filmToUpdate);

        var films = fetchFilms();
        assertThat(films).hasSize(1);
        var updatedFilm = films.getFirst();
        assertThat(updatedFilm).isEqualTo(filmToUpdate);
    }

    @Test
    void updateFilmUnknown_returnsNotFound() throws Exception {
        var unknownFilm = validFilmDto();
        unknownFilm.setId(9999L);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownFilm)))
                .andExpect(status().isNotFound());

        assertThat(fetchFilms()).isEmpty();
    }

    @Test
    void addFilm_withInvalidPayload() throws Exception {
        var invalidRequest = validFilmDto();
        invalidRequest.setDescription("a".repeat(201));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        assertThat(fetchFilms()).isEmpty();
    }

    @Test
    void putLike() throws Exception {
        var film = createFilm(validFilmDto());
        var userId = createUser(validUserDto());

        mockMvc.perform(put("/films/{id}/like/{userId}", film.getId(), userId))
                .andExpect(status().isOk());

        assertThat(filmStorage.getFilmById(film.getId()).getLikedUsers()).contains(userId);
    }

    @Test
    void deleteLike() throws Exception {
        var film = createFilm(validFilmDto());
        var userId = createUser(validUserDto());

        mockMvc.perform(put("/films/{id}/like/{userId}", film.getId(), userId))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/films/{id}/like/{userId}", film.getId(), userId))
                .andExpect(status().isOk());

        assertThat(filmStorage.getFilmById(film.getId()).getLikedUsers()).doesNotContain(userId);
    }

    @Test
    void likeNoFilm() throws Exception {
        var userId = createUser(validUserDto());

        mockMvc.perform(put("/films/{id}/like/{userId}", 9999L, userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void likeNoUser() throws Exception {
        var film = createFilm(validFilmDto());

        mockMvc.perform(put("/films/{id}/like/{userId}", film.getId(), 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void popular() throws Exception {
        var firstFilm = createFilm(validFilmDto());
        var secondFilm = createFilm(validFilmDto());
        var userId = createUser(validUserDto());
        var secondUserId = createUser(validUserDto());
        mockMvc.perform(put("/films/{id}/like/{userId}", secondFilm.getId(), secondUserId))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", secondFilm.getId(), userId))
                .andExpect(status().isOk());
        mockMvc.perform(put("/films/{id}/like/{userId}", firstFilm.getId(), userId))
                .andExpect(status().isOk());

        var popularFilmResponse = fetchPopularFilms();

        assertThat(popularFilmResponse.size()).isEqualTo(2);
        assertThat(popularFilmResponse.getFirst()).isEqualTo(secondFilm);
        assertThat(popularFilmResponse.getLast()).isEqualTo(firstFilm);
    }

    private FilmDto validFilmDto() {
        var dto = new FilmDto();
        dto.setName("Inception");
        dto.setDescription("A valid description");
        dto.setReleaseDate(LocalDate.of(2010, 7, 16));
        dto.setDuration(148);
        return dto;
    }

    private UserDto validUserDto() {
        var dto = new UserDto();
        dto.setEmail("john@example.com");
        dto.setLogin("john_doe");
        dto.setName("John Doe");
        dto.setBirthday(LocalDate.of(2000, 1, 1));
        return dto;
    }

    private List<FilmDto> fetchFilms() throws Exception {
        var response = mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }

    private FilmDto createFilm(FilmDto request) throws Exception {
        request.setId(null);
        var createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(createResponse, FilmDto.class);
    }

    private Long createUser(UserDto request) throws Exception {
        request.setId(null);
        var createResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(createResponse, UserDto.class).getId();
    }

    private List<FilmDto> fetchPopularFilms() throws Exception {
        var response = mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }
}
