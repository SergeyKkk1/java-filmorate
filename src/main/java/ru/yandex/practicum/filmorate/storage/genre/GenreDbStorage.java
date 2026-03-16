package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Repository(value = "genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> getGenres() {
        return jdbcTemplate.query(
                "SELECT id, name FROM genres ORDER BY id",
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                }
        );
    }

    @Override
    public Optional<Genre> getGenreById(Long id) {
        return jdbcTemplate.query("SELECT id, name FROM genres WHERE id = ?",
                        (rs, rowNum) -> {
                            Genre genre = new Genre();
                            genre.setId(rs.getLong("id"));
                            genre.setName(rs.getString("name"));
                            return genre;
                        }, id)
                .stream()
                .findFirst();
    }
}
