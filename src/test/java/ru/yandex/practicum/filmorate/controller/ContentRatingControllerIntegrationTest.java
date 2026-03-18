package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.ContentRatingDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ContentRatingControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getRatings_returnsSeededRatings() throws Exception {
        List<ContentRatingDto> ratings = fetchRatings();

        assertThat(ratings).hasSize(5);
        assertThat(ratings)
                .extracting(ContentRatingDto::getId, ContentRatingDto::getName)
                .contains(
                        tuple(1L, "G"),
                        tuple(2L, "PG"),
                        tuple(3L, "PG-13"),
                        tuple(4L, "R"),
                        tuple(5L, "NC-17")
                );
    }

    @Test
    void getRatingById_existing() throws Exception {
        ContentRatingDto rating = fetchRating(3L);

        assertThat(rating.getId()).isEqualTo(3L);
        assertThat(rating.getName()).isEqualTo("PG-13");
    }

    @Test
    void getRatingById_unknown_returnsNotFound() throws Exception {
        mockMvc.perform(get("/mpa/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    private List<ContentRatingDto> fetchRatings() throws Exception {
        String response = mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, new TypeReference<>() {
        });
    }

    private ContentRatingDto fetchRating(Long id) throws Exception {
        String response = mockMvc.perform(get("/mpa/{id}", id))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(response, ContentRatingDto.class);
    }
}
