package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.genre.GenreRowMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository(value = "genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    @Override
    public Collection<Genre> getGenres() {
        return jdbcTemplate.query(
                "SELECT id, name FROM genres ORDER BY id",
                genreRowMapper
        );
    }

    @Override
    public Optional<Genre> getGenreById(Long id) {
        return jdbcTemplate.query("SELECT id, name FROM genres WHERE id = ?",
                        genreRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Genre> getGenresByFilmId(Long filmId) {
        return jdbcTemplate.query(
                """
                SELECT g.id, g.name
                FROM film_genres fg
                JOIN genres g ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """,
                genreRowMapper,
                filmId
        );
    }

    @Override
    public Map<Long, List<Genre>> getFilmIdToGenres(List<Long> filmIds) {
        Map<Long, List<Genre>> result = new LinkedHashMap<>();
        if (filmIds == null || filmIds.isEmpty()) {
            return result;
        }

        filmIds.forEach(id -> result.put(id, new ArrayList<>()));

        String sql = """
            SELECT fg.film_id, g.id, g.name
            FROM film_genres fg
            JOIN genres g ON g.id = fg.genre_id
            WHERE fg.film_id IN (:filmIds)
            ORDER BY fg.film_id, g.id
            """;

        MapSqlParameterSource params = new MapSqlParameterSource("filmIds", filmIds);

        namedParameterJdbcTemplate.query(sql, params, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = genreRowMapper.mapRow(rs, rs.getRow());
            result.get(filmId).add(genre);
        });

        return result;
    }

    @Override
    public List<Genre> findGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = """
                SELECT id, name
                FROM genres
                WHERE id IN (:genreIds)
                ORDER BY id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("genreIds", genreIds);

        return namedParameterJdbcTemplate.query(sql, params, genreRowMapper);
    }
}
