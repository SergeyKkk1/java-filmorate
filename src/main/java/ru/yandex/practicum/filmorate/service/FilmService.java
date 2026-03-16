package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmRqDto;
import ru.yandex.practicum.filmorate.dto.FilmRsDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.ContentRatingNotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.content.ContentRatingStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

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
        return filmStorage.getFilms().stream()
                .map(this::toResponseDto)
                .toList();
    }

    public FilmRsDto getFilm(Long id) {
        log.info("Fetching film with id {}", id);
        return toResponseDto(getRequiredFilm(id));
    }

    public FilmRsDto addFilm(FilmRqDto filmDto) {
        Film film = filmMapper.map(filmDto);
        film.setGenres(normalizeGenres(film.getGenres()));
        validateFilmReferences(film);
        log.info("Adding film: {}", film.getName());
        return toResponseDto(filmStorage.addFilm(film));
    }

    public FilmRsDto updateFilm(FilmRqDto filmDto) {
        Film film = filmMapper.map(filmDto);
        film.setGenres(normalizeGenres(film.getGenres()));
        validateFilmReferences(film);
        log.info("Updating film: {} with id {}", film.getName(), film.getId());

        Film updatedFilm = filmStorage.getFilmById(film.getId())
                .orElseThrow(() -> new FilmNotFoundException(String.format("Film with id %s not found", film.getId())));
        updatedFilm.setDescription(film.getDescription());
        updatedFilm.setName(film.getName());
        updatedFilm.setReleaseDate(film.getReleaseDate());
        updatedFilm.setDuration(film.getDuration());
        updatedFilm.setContentRating(film.getContentRating());
        updatedFilm.setGenres(normalizeGenres(film.getGenres()));
        filmStorage.updateFilm(updatedFilm);
        return toResponseDto(updatedFilm);
    }

    public void clearFilms() {
        log.info("Clearing all films");
        filmStorage.clear();
    }

    public void addLike(Long id, Long userId) {
        log.info("Adding like to film {} from user {}", id, userId);
        Film film = getRequiredFilm(id);
        validateUserExists(userId);
        film.getLikedUsers().add(userId);
        filmStorage.updateFilm(film);
    }

    public void deleteLike(Long id, Long userId) {
        log.info("Deleting like from film {} by user {}", id, userId);
        Film film = getRequiredFilm(id);
        validateUserExists(userId);
        film.getLikedUsers().remove(userId);
        filmStorage.updateFilm(film);
    }

    public List<FilmRsDto> popular(int count) {
        log.info("Fetching {} popular films", count);
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparing((Film film) -> film.getLikedUsers().size(), Comparator.reverseOrder())
                        .thenComparing(Film::getId))
                .limit(count)
                .map(this::toResponseDto)
                .toList();
    }

    private Film getRequiredFilm(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new FilmNotFoundException(String.format("Film with id %s not found", id)));
    }

    private void validateUserExists(Long userId) {
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new UserNotFoundException(String.format("User with id %s not found", userId));
        }
    }

    private void validateFilmReferences(Film film) {
        if (film.getContentRating() != null && contentRatingStorage.getRatingById(film.getContentRating()).isEmpty()) {
            throw new ContentRatingNotFoundException(
                    String.format("Content rating with id %s not found", film.getContentRating())
            );
        }

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        for (Long genreId : film.getGenres()) {
            if (genreId == null || genreStorage.getGenreById(genreId).isEmpty()) {
                throw new GenreNotFoundException(String.format("Genre with id %s not found", genreId));
            }
        }
    }

    private List<Long> normalizeGenres(List<Long> genres) {
        if (genres == null || genres.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(new LinkedHashSet<>(genres));
    }

    private FilmRsDto toResponseDto(Film film) {
        FilmRsDto filmRsDto = filmMapper.mapToRsDto(film);

        if (filmRsDto.getMpa() != null && filmRsDto.getMpa().getId() != null) {
            String ratingName = contentRatingStorage.getRatingById(filmRsDto.getMpa().getId())
                    .orElseThrow(() -> new ContentRatingNotFoundException(
                            String.format("Content rating with id %s not found", filmRsDto.getMpa().getId())
                    ))
                    .getName();
            filmRsDto.getMpa().setName(ratingName);
        }

        if (filmRsDto.getGenres() == null) {
            filmRsDto.setGenres(new ArrayList<>());
            return filmRsDto;
        }

        for (GenreDto genreDto : filmRsDto.getGenres()) {
            if (genreDto == null || genreDto.getId() == null) {
                continue;
            }
            String genreName = genreStorage.getGenreById(genreDto.getId())
                    .orElseThrow(() -> new GenreNotFoundException(
                            String.format("Genre with id %s not found", genreDto.getId())
                    ))
                    .getName();
            genreDto.setName(genreName);
        }
        return filmRsDto;
    }
}
