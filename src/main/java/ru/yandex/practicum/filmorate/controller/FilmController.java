package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.FilmIdIsNullException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public List<FilmDto> getFilms() {
        return filmService.getFilms();
    }

    @PostMapping
    public FilmDto addFilm(@Valid @RequestBody FilmDto film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody FilmDto film) {
        if (film.getId() == null) {
            throw new FilmIdIsNullException("Film id is null");
        }
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        if (id == null) {
            throw new FilmIdIsNullException("Film id is null");
        }
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        if (id == null) {
            throw new FilmIdIsNullException("Film id is null");
        }
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> popular(@RequestParam(defaultValue = "10") int count) {
        if (count <= 0) {
            throw new ValidationException("Count must be positive");
        }
        return filmService.popular(count);
    }
}
