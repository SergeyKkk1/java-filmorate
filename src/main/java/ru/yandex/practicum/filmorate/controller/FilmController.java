package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;
    private final FilmMapper filmMapper;

    @GetMapping
    public List<FilmDto> getFilms() {
        return filmService.getFilms().stream().map(filmMapper::mapToDto).toList();
    }

    @PostMapping
    public FilmDto addFilm(@Valid @RequestBody FilmDto film) {
        return filmMapper.mapToDto(filmService.addFilm(filmMapper.map(film)));
    }

    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody FilmDto film) {
        if (film.getId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Film id is null");
        }
        return filmMapper.mapToDto(filmService.updateFilm(filmMapper.map(film)));
    }
}
