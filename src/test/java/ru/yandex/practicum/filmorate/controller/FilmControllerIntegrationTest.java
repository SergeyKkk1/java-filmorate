package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @BeforeEach
    void setUp() {
        filmService.getFilms().clear();
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
    void addFilm_withInvalidPayload() throws Exception {
        var invalidRequest = validFilmDto();
        invalidRequest.setDescription("a".repeat(201));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        assertThat(fetchFilms()).isEmpty();
    }

    private FilmDto validFilmDto() {
        var dto = new FilmDto();
        dto.setName("Inception");
        dto.setDescription("A valid description");
        dto.setReleaseDate(LocalDate.of(2010, 7, 16));
        dto.setDuration(148);
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
}
