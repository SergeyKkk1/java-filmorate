package ru.yandex.practicum.filmorate.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import ru.yandex.practicum.filmorate.dto.FilmRqDto;
import ru.yandex.practicum.filmorate.dto.FilmRsDto;
import ru.yandex.practicum.filmorate.model.Film;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface FilmMapper {
    @Mapping(target = "mpa", source = "contentRating")
    FilmRsDto mapToRsDto(Film film);

    @Mapping(target = "contentRating", source = "mpa")
    @Mapping(target = "likedUsers", ignore = true)
    Film map(FilmRqDto filmRqDto);
}
