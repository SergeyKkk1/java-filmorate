package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.ContentRatingDto;
import ru.yandex.practicum.filmorate.dto.mapper.ContentRatingMapper;
import ru.yandex.practicum.filmorate.exception.ContentRatingNotFoundException;
import ru.yandex.practicum.filmorate.storage.content.ContentRatingStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentRatingService {
    private final ContentRatingStorage contentRatingStorage;
    private final ContentRatingMapper contentRatingMapper;

    public List<ContentRatingDto> getRatings() {
        return contentRatingStorage.getRatings().stream()
                .map(contentRatingMapper::mapToDto)
                .toList();
    }

    public ContentRatingDto getRating(Long id) {
        return contentRatingStorage.getRatingById(id)
                .map(contentRatingMapper::mapToDto)
                .orElseThrow(() -> new ContentRatingNotFoundException(
                        String.format("Content rating with id %s not found", id)
                ));
    }
}
