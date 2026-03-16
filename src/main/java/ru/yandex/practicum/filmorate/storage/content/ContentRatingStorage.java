package ru.yandex.practicum.filmorate.storage.content;

import ru.yandex.practicum.filmorate.model.ContentRating;

import java.util.Collection;
import java.util.Optional;

public interface ContentRatingStorage {
    Collection<ContentRating> getRatings();

    Optional<ContentRating> getRatingById(Long id);
}
