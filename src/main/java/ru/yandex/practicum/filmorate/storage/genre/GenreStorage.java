package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GenreStorage {
    Collection<Genre> getGenres();

    Optional<Genre> getGenreById(Long id);

    List<Genre> getGenresByFilmId(Long filmId);

    Map<Long, List<Genre>> getFilmIdToGenres(List<Long> filmIds);

    List<Genre> findGenres(List<Long> genreIds);
}
