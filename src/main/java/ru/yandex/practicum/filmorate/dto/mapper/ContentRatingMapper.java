package ru.yandex.practicum.filmorate.dto.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.filmorate.dto.ContentRatingDto;
import ru.yandex.practicum.filmorate.model.ContentRating;

@Mapper
public interface ContentRatingMapper {
    ContentRatingDto mapToDto(ContentRating contentRating);

    ContentRating map(ContentRatingDto contentRatingDto);
}
