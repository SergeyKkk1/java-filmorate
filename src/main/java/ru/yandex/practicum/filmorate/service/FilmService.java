package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmRqDto;
import ru.yandex.practicum.filmorate.dto.FilmRsDto;
import ru.yandex.practicum.filmorate.dto.IdDto;
import ru.yandex.practicum.filmorate.dto.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.ContentRatingNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.ContentRating;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.content.ContentRatingStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("genreDbStorage")
    private final GenreStorage genreStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final ContentRatingStorage contentRatingStorage;
    private final FilmMapper filmMapper;

    public List<FilmRsDto> getFilms() {
        log.info("Fetching all films");
        Collection<Film> films = filmStorage.getFilms();
        List<Long> filmIds = films.stream().map(Film::getId).toList();
        Map<Long, List<User>> filmIdToLikedUsers = filmStorage.getFilmIdToLikedUsers(filmIds);
        Map<Long, List<Genre>> filmIdToGenres = genreStorage.getFilmIdToGenres(filmIds);
        return films.stream()
                .map(film -> film.setLikedUsers(filmIdToLikedUsers.get(film.getId())))
                .map(film -> film.setGenres(filmIdToGenres.get(film.getId())))
                .map(filmMapper::mapToRsDto)
                .toList();
    }

    public FilmRsDto getFilm(Long id) {
        log.info("Fetching film with id {}", id);
        return filmMapper.mapToRsDto(getRequiredFilm(id));
    }

    public FilmRsDto addFilm(FilmRqDto filmDto) {
        Film film = filmMapper.map(filmDto);
        log.info("Adding film: {}", film.getName());
        setFilmGenres(film, filmDto);
        setFilmContentRating(film, filmDto);
        return filmMapper.mapToRsDto(filmStorage.addFilm(film));
    }

    public FilmRsDto updateFilm(FilmRqDto filmDto) {
        Film film = filmMapper.map(filmDto);
        setFilmGenres(film, filmDto);
        setFilmContentRating(film, filmDto);
        log.info("Updating film: {} with id {}", film.getName(), film.getId());
        Film updatedFilm = filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> new FilmNotFoundException(String.format("Film with id %s not found", film.getId())));
        updatedFilm.setDescription(film.getDescription());
        updatedFilm.setName(film.getName());
        updatedFilm.setReleaseDate(film.getReleaseDate());
        updatedFilm.setDuration(film.getDuration());
        updatedFilm.setContentRating(film.getContentRating());
        updatedFilm.setGenres(film.getGenres());
        filmStorage.updateFilm(updatedFilm);
        return filmMapper.mapToRsDto(updatedFilm);
    }

    private void setFilmGenres(Film film, FilmRqDto filmDto) {
        List<Long> genreIds = filmDto.getGenres() == null ? List.of() : filmDto.getGenres().stream().map(IdDto::getId).toList();
        List<Genre> genres = genreStorage.findGenres(genreIds);
        validateFilmReferences(film, genres);
        film.setGenres(genres);
    }

    public void clearFilms() {
        log.info("Clearing all films");
        filmStorage.clear();
    }

    public void addLike(Long id, Long userId) {
        log.info("Adding like to film {} from user {}", id, userId);
        validateFilmExists(id);
        validateUserExists(userId);
        filmStorage.addLike(id, userId);
    }

    public void deleteLike(Long id, Long userId) {
        log.info("Deleting like from film {} by user {}", id, userId);
        validateFilmExists(id);
        validateUserExists(userId);
        filmStorage.removeLike(id, userId);
    }

    public List<FilmRsDto> popular(int count) {
        log.info("Fetching {} popular films", count);
        Collection<Film> films = filmStorage.getPopularFilms(count);
        List<Long> filmIds = films.stream().map(Film::getId).toList();
        Map<Long, List<Genre>> filmIdToGenres = genreStorage.getFilmIdToGenres(filmIds);
        Map<Long, List<User>> filmIdToLikedUsers = filmStorage.getFilmIdToLikedUsers(filmIds);
        return films.stream()
                .map(film -> film.setGenres(filmIdToGenres.get(film.getId())))
                .map(film -> film.setLikedUsers(filmIdToLikedUsers.get(film.getId())))
                .map(filmMapper::mapToRsDto)
                .toList();
    }

    private Film getRequiredFilm(Long id) {
        Film film = filmStorage.getFilmById(id)
                .orElseThrow(() -> new FilmNotFoundException(String.format("Film with id %s not found", id)));
        film.setGenres(genreStorage.getGenresByFilmId(id));
        return film;
    }

    private void validateUserExists(Long userId) {
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new UserNotFoundException(String.format("User with id %s not found", userId));
        }
    }

    private void validateFilmExists(Long filmId) {
        if (filmStorage.getFilmById(filmId).isEmpty()) {
            throw new FilmNotFoundException(String.format("Film with id %s not found", filmId));
        }
    }

    private void validateFilmReferences(Film film, List<Genre> genres) {
        if (film.getContentRating() != null && contentRatingStorage.getRatingById(film.getContentRating().getId()).isEmpty()) {
            throw new ContentRatingNotFoundException(
                    String.format("Content rating with id %s not found", film.getContentRating())
            );
        }

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        if (film.getGenres().size() != genres.size()) {
            throw new GenreNotFoundException("Some of genres not found");
        }
    }

    private void setFilmContentRating(Film film, FilmRqDto filmDto) {
        if (filmDto.getMpa() != null) {
            ContentRating rating = contentRatingStorage.getRatingById(filmDto.getMpa().getId()).orElseThrow(() -> new ContentRatingNotFoundException(
                    String.format("Content rating with id %s not found", filmDto.getMpa().getId())
            ));
            film.setContentRating(rating);
        }
    }
}
