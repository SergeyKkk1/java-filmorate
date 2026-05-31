package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.GenreDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GenresControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getGenres_returnsSeededGenres() throws Exception {
        List<GenreDto> genres = fetchGenres();

        assertThat(genres).hasSize(6);
        assertThat(genres)
                .extracting(GenreDto::getId, GenreDto::getName)
                .contains(
                        tuple(1L, "Комедия"),
                        tuple(2L, "Драма"),
                        tuple(3L, "Мультфильм"),
                        tuple(4L, "Триллер"),
                        tuple(5L, "Документальный"),
                        tuple(6L, "Боевик")
                );
    }

    @Test
    void getGenreById_existing() throws Exception {
        GenreDto genre = fetchGenre(4L);

        assertThat(genre.getId()).isEqualTo(4L);
        assertThat(genre.getName()).isEqualTo("Триллер");
    }

    @Test
    void getGenreById_unknown_returnsNotFound() throws Exception {
        mockMvc.perform(get("/genres/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    private List<GenreDto> fetchGenres() throws Exception {
        String response = mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }

    private GenreDto fetchGenre(Long id) throws Exception {
        String response = mockMvc.perform(get("/genres/{id}", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, GenreDto.class);
    }
}
