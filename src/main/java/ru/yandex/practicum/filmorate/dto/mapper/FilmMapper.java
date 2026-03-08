package ru.yandex.practicum.filmorate.dto.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

@Mapper
public interface FilmMapper {
    FilmDto mapToDto(Film film);

    Film map(FilmDto filmDto);
}
