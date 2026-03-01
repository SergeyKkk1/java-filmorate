package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.dto.validation.After;

import java.time.LocalDate;

@Data
public class FilmDto {
    private Long id;
    @NotEmpty
    private String name;
    @Size(max = 200)
    private String description;
    @After("1895-12-28")
    private LocalDate releaseDate;
    @Positive
    private Integer duration;
}
