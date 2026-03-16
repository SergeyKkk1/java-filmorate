package ru.yandex.practicum.filmorate.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.yandex.practicum.filmorate.dto.ContentRatingDto;
import ru.yandex.practicum.filmorate.dto.FilmRqDto;
import ru.yandex.practicum.filmorate.dto.FilmRsDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.IdDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper
public interface FilmMapper {
    @Mapping(target = "mpa", source = "contentRating", qualifiedByName = "toContentRatingDto")
    @Mapping(target = "genres", source = "genres", qualifiedByName = "toGenreDtoList")
    FilmRsDto mapToRsDto(Film film);

    @Mapping(target = "contentRating", source = "mpa", qualifiedByName = "toId")
    @Mapping(target = "genres", source = "genres", qualifiedByName = "toIdList")
    Film map(FilmRqDto filmRqDto);

    @Named("toContentRatingDto")
    default ContentRatingDto toContentRatingDto(Long id) {
        if (id == null) {
            return null;
        }
        ContentRatingDto dto = new ContentRatingDto();
        dto.setId(id);
        return dto;
    }

    @Named("toGenreDtoList")
    default List<GenreDto> toGenreDtoList(List<Long> ids) {
        List<GenreDto> result = new ArrayList<>();
        if (ids == null) {
            return result;
        }
        for (Long id : ids) {
            if (id != null) {
                GenreDto dto = new GenreDto();
                dto.setId(id);
                result.add(dto);
            }
        }
        return result;
    }

    @Named("toId")
    default Long toId(IdDto dto) {
        if (dto == null) {
            return null;
        }
        return dto.getId();
    }

    @Named("toIdList")
    default List<Long> toIdList(Set<IdDto> ids) {
        List<Long> result = new ArrayList<>();
        if (ids == null) {
            return result;
        }
        for (IdDto idDto : ids) {
            if (idDto != null && idDto.getId() != null) {
                result.add(idDto.getId());
            }
        }
        result.sort(Comparator.naturalOrder());
        return result;
    }
}
