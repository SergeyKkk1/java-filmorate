package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.user.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository(value = "filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final UserRowMapper userRowMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Collection<Film> getFilms() {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       cr.id   AS cr_id,
                       cr.name AS cr_name
                FROM films f
                LEFT JOIN content_ratings cr ON cr.id = f.content_rating_id
                ORDER BY f.id
                """;
        return jdbcTemplate.query(sql, filmRowMapper);
    }

    @Override
    public Film addFilm(Film film) {
        String query = "INSERT INTO films(name, description, release_date, duration, content_rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Long ratingId = film.getContentRating() == null ? null : film.getContentRating().getId();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(query, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate(), Types.DATE);
            ps.setInt(4, film.getDuration());
            ps.setObject(5, ratingId, Types.BIGINT);
            return ps;
        }, keyHolder);
        film.setId(keyHolder.getKey().longValue());
        updateGenres(film);
        return film;
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        Optional<Film> film = jdbcTemplate.query(
                        "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                                "cr.id AS cr_id, cr.name AS cr_name " +
                                "FROM films f " +
                                "LEFT JOIN content_ratings cr ON cr.id = f.content_rating_id " +
                                "WHERE f.id = ?",
                        filmRowMapper,
                        id
                )
                .stream()
                .findFirst();
        film.ifPresent(this::fillFilmLikes);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, content_rating_id = ? WHERE id = ?";
        Long ratingId = film.getContentRating() == null ? null : film.getContentRating().getId();
        int updated = jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), ratingId, film.getId());
        if (updated == 0) {
            throw new FilmNotFoundException(String.format("Film with id %s not found", film.getId()));
        }
        updateGenres(film);
        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "INSERT INTO film_likes (film_id, user_id) " +
                        "SELECT ?, ? WHERE NOT EXISTS (" +
                        "SELECT 1 FROM film_likes WHERE film_id = ? AND user_id = ?)",
                filmId,
                userId,
                filmId,
                userId
        );
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        return jdbcTemplate.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                        "cr.id AS cr_id, cr.name AS cr_name " +
                        "FROM films f " +
                        "LEFT JOIN content_ratings cr ON cr.id = f.content_rating_id " +
                        "ORDER BY (" +
                        "SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id" +
                        ") DESC, f.id " +
                        "LIMIT ?",
                filmRowMapper,
                count
        );
    }

    @Override
    public void clear() {
        jdbcTemplate.update("DELETE FROM films");
    }

    private List<User> fillFilmLikes(Film film) {
        List<User> likedUsers = jdbcTemplate.query(
                "SELECT u.id, u.name, u.login, u.email, u.birthday " +
                        "FROM film_likes fl " +
                        "JOIN users u ON u.id = fl.user_id " +
                        "WHERE fl.film_id = ? " +
                        "ORDER BY u.id",
                userRowMapper,
                film.getId()
        );
        film.setLikedUsers(likedUsers);
        return likedUsers;
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
                (ps, genre) -> {
                    ps.setLong(1, film.getId());
                    ps.setLong(2, genre.getId());
                }
        );
    }

    @Override
    public Map<Long, List<User>> getFilmIdToLikedUsers(List<Long> filmIds) {
        Map<Long, List<User>> result = new LinkedHashMap<>();
        if (filmIds == null || filmIds.isEmpty()) {
            return result;
        }

        filmIds.forEach(id -> result.put(id, new ArrayList<>()));

        String sql = """
                SELECT fl.film_id, u.id, u.name, u.login, u.email, u.birthday
                FROM film_likes fl
                JOIN users u ON u.id = fl.user_id
                WHERE fl.film_id IN (:filmIds)
                ORDER BY fl.film_id, u.id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("filmIds", filmIds);

        namedParameterJdbcTemplate.query(sql, params, rs -> {
            Long filmId = rs.getLong("film_id");
            User user = userRowMapper.mapRow(rs, rs.getRow());
            result.get(filmId).add(user);
        });

        return result;
    }
}
