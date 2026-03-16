package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mapper.film.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@Repository(value = "filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Collection<Film> getFilms() {
        Collection<Film> films = jdbcTemplate.query("SELECT * FROM films ORDER BY id", filmRowMapper);
        films.forEach(this::fillFilmRelations);
        return films;
    }

    @Override
    public Film addFilm(Film film) {
        String query = "INSERT INTO films(name, description, release_date, duration, content_rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(query, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate(), Types.DATE);
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getContentRating(), Types.BIGINT);
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        updateGenres(film);
        return film;
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        Optional<Film> film = jdbcTemplate.query("SELECT * FROM films WHERE id = ?", filmRowMapper, id)
                .stream()
                .findFirst();
        film.ifPresent(this::fillFilmRelations);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, content_rating_id = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getContentRating(), film.getId());
        if (updated == 0) {
            throw new FilmNotFoundException(String.format("Film with id %s not found", film.getId()));
        }
        updateGenres(film);
        updateLikes(film);
        return film;
    }

    @Override
    public void clear() {
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
    }

    private void fillFilmRelations(Film film) {
        fillFilmLikes(film);
        fillFilmGenres(film);
    }

    private void fillFilmLikes(Film film) {
        film.setLikedUsers(new HashSet<>(jdbcTemplate.queryForList(
                "SELECT user_id FROM film_likes WHERE film_id = ? ORDER BY user_id",
                Long.class,
                film.getId()
        )));
    }

    private void fillFilmGenres(Film film) {
        film.setGenres(jdbcTemplate.queryForList(
                "SELECT genre_id FROM film_genres WHERE film_id = ? ORDER BY genre_id",
                Long.class,
                film.getId()
        ));
    }

    private void updateLikes(Film film) {
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ?", film.getId());
        if (film.getLikedUsers() == null || film.getLikedUsers().isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO film_likes(film_id, user_id) VALUES (?, ?)",
                film.getLikedUsers(),
                film.getLikedUsers().size(),
                (ps, userId) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, userId);
                }
        );
    }

    private void updateGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)",
                film.getGenres(),
                film.getGenres().size(),
                (ps, genreId) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, genreId);
                }
        );
    }
}
