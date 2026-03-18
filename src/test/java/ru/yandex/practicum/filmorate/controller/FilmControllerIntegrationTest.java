package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.FilmRqDto;
import ru.yandex.practicum.filmorate.dto.FilmRsDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.IdDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Qualifier("filmDbStorage")
    private FilmStorage filmStorage;

    private int userSeq;

    @BeforeEach
    void setUp() {
        filmService.clearFilms();
        userService.clearUsers();
        userSeq = 0;
    }

    @Test
    void getFilms_emptyList() throws Exception {
        var films = fetchFilms();
        assertThat(films).isEmpty();
    }

    @Test
    void addFilm() throws Exception {
        var request = validFilmRqDto();
        request.setId(null);

        var createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var createdFilm = objectMapper.readValue(createResponse, FilmRsDto.class);
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo(request.getName());
        assertThat(createdFilm.getDescription()).isEqualTo(request.getDescription());
        assertThat(createdFilm.getReleaseDate()).isEqualTo(request.getReleaseDate());
        assertThat(createdFilm.getDuration()).isEqualTo(request.getDuration());
        assertThat(createdFilm.getMpa()).isNotNull();
        assertThat(createdFilm.getMpa().getId()).isEqualTo(request.getMpa().getId());
        assertThat(createdFilm.getMpa().getName()).isNotBlank();
        assertThat(createdFilm.getGenres())
                .extracting(GenreDto::getId)
                .containsExactlyInAnyOrderElementsOf(request.getGenres().stream().map(IdDto::getId).toList());
        assertThat(createdFilm.getGenres())
                .allMatch(genre -> genre.getName() != null && !genre.getName().isBlank());

        var films = fetchFilms();
        assertThat(films).hasSize(1);
        assertThat(films.getFirst()).isEqualTo(createdFilm);
    }

    @Test
    void addFilmNoGenreNoMpa() throws Exception {
        var request = validFilmRqDto().setId(null).setMpa(null).setGenres(null);

        var createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var createdFilm = objectMapper.readValue(createResponse, FilmRsDto.class);
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo(request.getName());
        assertThat(createdFilm.getDescription()).isEqualTo(request.getDescription());
        assertThat(createdFilm.getReleaseDate()).isEqualTo(request.getReleaseDate());
        assertThat(createdFilm.getDuration()).isEqualTo(request.getDuration());
        assertThat(createdFilm.getMpa()).isNull();
        assertThat(createdFilm.getGenres()).isEmpty();

        var films = fetchFilms();
        assertThat(films).hasSize(1);
        assertThat(films.getFirst()).isEqualTo(createdFilm);
    }

    @Test
    void getFilmById_returnsGenres() throws Exception {
        var createdFilm = createFilm(validFilmRqDto());

        var response = mockMvc.perform(get("/films/{id}", createdFilm.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var film = objectMapper.readValue(response, FilmRsDto.class);
        assertThat(film.getId()).isEqualTo(createdFilm.getId());
        assertThat(film.getGenres())
                .extracting(GenreDto::getId)
                .containsExactlyInAnyOrder(1L, 4L);
        assertThat(film.getGenres())
                .allMatch(genre -> genre.getName() != null && !genre.getName().isBlank());
    }

    @Test
    void updateFilm() throws Exception {
        var createRequest = validFilmRqDto();
        createRequest.setId(null);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        var createdFilm = fetchFilms().getFirst();
        FilmRqDto filmToUpdate = validFilmRqDto()
                .setId(createdFilm.getId())
                .setName("Updated Name")
                .setDescription("Updated description")
                .setReleaseDate(LocalDate.of(2024, 2, 1))
                .setDuration(95)
                .setMpa(new IdDto().setId(4L))
                .setGenres(Set.of(new IdDto().setId(2L), new IdDto().setId(6L)));

        var updateResponse = mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmToUpdate)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var updatedFilmFromResponse = objectMapper.readValue(updateResponse, FilmRsDto.class);
        assertThat(updatedFilmFromResponse.getId()).isEqualTo(filmToUpdate.getId());
        assertThat(updatedFilmFromResponse.getName()).isEqualTo(filmToUpdate.getName());
        assertThat(updatedFilmFromResponse.getDescription()).isEqualTo(filmToUpdate.getDescription());
        assertThat(updatedFilmFromResponse.getReleaseDate()).isEqualTo(filmToUpdate.getReleaseDate());
        assertThat(updatedFilmFromResponse.getDuration()).isEqualTo(filmToUpdate.getDuration());
        assertThat(updatedFilmFromResponse.getMpa().getId()).isEqualTo(filmToUpdate.getMpa().getId());
        assertThat(updatedFilmFromResponse.getGenres())
                .extracting(GenreDto::getId)
                .containsExactlyInAnyOrderElementsOf(filmToUpdate.getGenres().stream().map(IdDto::getId).toList());

        var films = fetchFilms();
        assertThat(films).hasSize(1);
        var updatedFilm = films.getFirst();
        assertThat(updatedFilm).isEqualTo(updatedFilmFromResponse);
    }

    @Test
    void updateFilmUnknown_returnsNotFound() throws Exception {
        var unknownFilm = validFilmRqDto();
        unknownFilm.setId(9999L);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownFilm)))
                .andExpect(status().isNotFound());

        assertThat(fetchFilms()).isEmpty();
    }

    @Test
    void addFilm_withInvalidPayload() throws Exception {
        var invalidRequest = validFilmRqDto();
        invalidRequest.setDescription("a".repeat(201));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        assertThat(fetchFilms()).isEmpty();
    }

    @Test
    void putLike() throws Exception {
        var film = createFilm(validFilmRqDto());
        var userId = createUser(validUserDto());

        mockMvc.perform(put("/films/{id}/like/{userId}", film.getId(), userId))
                .andExpect(status().isOk());

        Set<Long> actualLikedUserIds = filmStorage.getFilmById(film.getId()).orElseThrow().getLikedUsers().stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        assertThat(actualLikedUserIds).contains(userId);
    }

    @Test
    void deleteLike() throws Exception {
        var film = createFilm(validFilmRqDto());
        var userId = createUser(validUserDto());

        mockMvc.perform(put("/films/{id}/like/{userId}", film.getId(), userId))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/films/{id}/like/{userId}", film.getId(), userId))
                .andExpect(status().isOk());

        Set<Long> actualLikedUserIds = filmStorage.getFilmById(film.getId()).orElseThrow().getLikedUsers().stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        assertThat(actualLikedUserIds).doesNotContain(userId);
    }

    @Test
    void likeNoFilm() throws Exception {
        var userId = createUser(validUserDto());

        mockMvc.perform(put("/films/{id}/like/{userId}", 9999L, userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void likeNoUser() throws Exception {
        var film = createFilm(validFilmRqDto());

        mockMvc.perform(put("/films/{id}/like/{userId}", film.getId(), 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void popular() throws Exception {
        var firstFilm = createFilm(validFilmRqDto());
        var secondFilm = createFilm(validFilmRqDto());
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

    @Test
    void popular_zeroCount() throws Exception {
        mockMvc.perform(get("/films/popular").param("count", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void popular_emptyList() throws Exception {
        var films = fetchPopularFilms();
        assertThat(films).isEmpty();
    }

    private FilmRqDto validFilmRqDto() {
        var dto = new FilmRqDto();
        dto.setName("Inception");
        dto.setDescription("A valid description");
        dto.setReleaseDate(LocalDate.of(2010, 7, 16));
        dto.setDuration(148);
        dto.setMpa(new IdDto().setId(3L));
        dto.setGenres(Set.of(new IdDto().setId(1L), new IdDto().setId(4L)));
        return dto;
    }

    private UserDto validUserDto() {
        int id = ++userSeq;
        var dto = new UserDto();
        dto.setEmail("john" + id + "@example.com");
        dto.setLogin("john_" + id);
        dto.setName("John Doe");
        dto.setBirthday(LocalDate.of(2000, 1, 1));
        return dto;
    }

    private List<FilmRsDto> fetchFilms() throws Exception {
        var response = mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }

    private FilmRsDto createFilm(FilmRqDto request) throws Exception {
        request.setId(null);
        var createResponse = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(createResponse, FilmRsDto.class);
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

    private List<FilmRsDto> fetchPopularFilms() throws Exception {
        var response = mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }
}
