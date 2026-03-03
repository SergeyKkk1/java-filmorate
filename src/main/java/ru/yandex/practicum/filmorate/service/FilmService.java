package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final Map<Long, Film> films = new LinkedHashMap<>();
    private final FilmMapper filmMapper;
    private long nextId = 1;

    public List<FilmDto> getFilms() {
        return films.values().stream()
                .map(filmMapper::mapToDto)
                .toList();
    }

    public FilmDto addFilm(FilmDto filmDto) {
        Film film = filmMapper.map(filmDto);
        log.info("Adding film: {}", film.getName());
        if (film.getId() == null) {
            film.setId(nextId++);
        }
        films.put(film.getId(), film);
        return filmMapper.mapToDto(film);
    }

    public FilmDto updateFilm(FilmDto filmDto) {
        Film film = filmMapper.map(filmDto);
        log.info("Updating film: {} with id {}", film.getName(), film.getId());

        Film updatedFilm = films.get(film.getId());
        if (updatedFilm == null) {
            throw new FilmNotFoundException("Film not found");
        }
        updatedFilm.setDescription(film.getDescription());
        updatedFilm.setName(film.getName());
        updatedFilm.setReleaseDate(film.getReleaseDate());
        updatedFilm.setDuration(film.getDuration());
        return filmMapper.mapToDto(updatedFilm);
    }

    public void clearFilms() {
        films.clear();
        nextId = 1;
    }
}
