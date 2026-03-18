package ru.yandex.practicum.filmorate.dto.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

@Mapper
public interface GenreMapper {
    GenreDto mapToDto(Genre genre);

    Genre map(GenreDto genreDto);
}
