package ru.yandex.practicum.filmorate.storage.mapper.film;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.ContentRating;
import ru.yandex.practicum.filmorate.model.Film;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Nullable
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long crId = rs.getObject("cr_id", Long.class);
        ContentRating rating = null;
        if (crId != null) {
            rating = new ContentRating();
            rating.setId(crId);
            rating.setName(rs.getString("cr_name"));
        }
        return new Film()
                .setId(rs.getLong("id"))
                .setName(rs.getString("name"))
                .setDescription(rs.getString("description"))
                .setDuration(rs.getInt("duration"))
                .setContentRating(rating)
                .setReleaseDate(rs.getObject("release_date", LocalDate.class));
    }
}
