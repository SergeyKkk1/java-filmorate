package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FilmService {
    private final List<Film> films = new ArrayList<>();
    private long nextId = 1;

    public List<Film> getFilms() {
        return films;
    }

    public Film addFilm(Film film) {
        log.info("Adding film: {}", film.getName());
        if (film.getId() == null) {
            film.setId(nextId++);
        }
        films.add(film);
        return film;
    }

    public Film updateFilm(Film film) {
        log.info("Updating film: {} with id {}", film.getName(), film.getId());
        if (film.getId() == null) {
            return film;
        }
        Optional<Film> foundFilm = films.stream()
                .filter(f -> film.getId().equals(f.getId()))
                .findFirst();
        if (foundFilm.isPresent()) {
            var updatedFilm = foundFilm.get();
            updatedFilm.setDescription(film.getDescription());
            updatedFilm.setName(film.getName());
            updatedFilm.setReleaseDate(film.getReleaseDate());
            updatedFilm.setDuration(film.getDuration());
            return updatedFilm;
        }
        return film;
    }
}
