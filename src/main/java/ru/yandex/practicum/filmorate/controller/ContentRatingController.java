package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.ContentRatingDto;
import ru.yandex.practicum.filmorate.service.ContentRatingService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class ContentRatingController {
    private final ContentRatingService contentRatingService;

    @GetMapping
    public List<ContentRatingDto> getRatings() {
        return contentRatingService.getRatings();
    }

    @GetMapping("/{id}")
    public ContentRatingDto getRating(@PathVariable Long id) {
        return contentRatingService.getRating(id);
    }
}
