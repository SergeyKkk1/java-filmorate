package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.yandex.practicum.filmorate.dto.validation.After;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Accessors(chain = true)
public class FilmRqDto {
    private Long id;
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;
    @After("1895-12-28")
    @NotNull
    private LocalDate releaseDate;
    @Positive
    @NotNull
    private Integer duration;

    private IdDto mpa;
    private Set<IdDto> genres = new HashSet<>();
}
