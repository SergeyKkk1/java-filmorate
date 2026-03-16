package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {
    @Qualifier("genreDbStorage")
    private final GenreStorage genreStorage;
    private final GenreMapper genreMapper;

    public List<GenreDto> getGenres() {
        log.info("Fetching genres");
        return genreStorage.getGenres().stream()
                .map(genreMapper::mapToDto)
                .toList();
    }

    public GenreDto getGenre(Long id) {
        log.info("Fetching genre with id {}", id);
        return genreStorage.getGenreById(id).map(genreMapper::mapToDto)
                .orElseThrow(() -> new GenreNotFoundException(String.format("Genre with id %s not found", id)));
    }
}
