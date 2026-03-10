package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FilmMapper filmMapper;

    public List<FilmDto> getFilms() {
        return filmStorage.getFilms().stream()
                .map(filmMapper::mapToDto)
                .toList();
    }

    public FilmDto addFilm(FilmDto filmDto) {
        Film film = filmMapper.map(filmDto);
        log.info("Adding film: {}", film.getName());
        return filmMapper.mapToDto(filmStorage.addFilm(film));
    }

    public FilmDto updateFilm(FilmDto filmDto) {
        Film film = filmMapper.map(filmDto);
        log.info("Updating film: {} with id {}", film.getName(), film.getId());

        Film updatedFilm = filmStorage.getFilmById(film.getId());
        if (updatedFilm == null) {
            throw new FilmNotFoundException(String.format("Film with id %s not found", film.getId()));
        }
        updatedFilm.setDescription(film.getDescription());
        updatedFilm.setName(film.getName());
        updatedFilm.setReleaseDate(film.getReleaseDate());
        updatedFilm.setDuration(film.getDuration());
        filmStorage.updateFilm(updatedFilm);
        return filmMapper.mapToDto(updatedFilm);
    }

    public void clearFilms() {
        filmStorage.clear();
    }

    public void addLike(Long id, Long userId) {
        Film film = getRequiredFilm(id);
        validateUserExists(userId);
        film.getLikedUsers().add(userId);
        filmStorage.updateFilm(film);
    }

    public void deleteLike(Long id, Long userId) {
        Film film = getRequiredFilm(id);
        validateUserExists(userId);
        film.getLikedUsers().remove(userId);
        filmStorage.updateFilm(film);
    }

    private Film getRequiredFilm(Long id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            throw new FilmNotFoundException(String.format("Film with id %s not found", id));
        }
        return film;
    }

    private void validateUserExists(Long userId) {
        if (userStorage.getUserById(userId) == null) {
            throw new UserNotFoundException(String.format("User with id %s not found", userId));
        }
    }

    public List<FilmDto> popular(int count) {
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparing(film -> film.getLikedUsers().size(), Comparator.reverseOrder()))
                .limit(count)
                .map(filmMapper::mapToDto)
                .toList();
    }
}
