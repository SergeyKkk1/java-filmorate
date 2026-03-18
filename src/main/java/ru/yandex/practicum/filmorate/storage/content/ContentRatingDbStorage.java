package ru.yandex.practicum.filmorate.storage.content;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.ContentRating;

import java.util.Collection;
import java.util.Optional;

@Repository(value = "contentRatingDbStorage")
@RequiredArgsConstructor
public class ContentRatingDbStorage implements ContentRatingStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<ContentRating> getRatings() {
        return jdbcTemplate.query(
                "SELECT id, name FROM content_ratings ORDER BY id",
                (rs, rowNum) -> {
                    ContentRating contentRating = new ContentRating();
                    contentRating.setId(rs.getLong("id"));
                    contentRating.setName(rs.getString("name"));
                    return contentRating;
                }
        );
    }

    @Override
    public Optional<ContentRating> getRatingById(Long id) {
        return jdbcTemplate.query(
                        "SELECT id, name FROM content_ratings WHERE id = ?",
                        (rs, rowNum) -> {
                            ContentRating contentRating = new ContentRating();
                            contentRating.setId(rs.getLong("id"));
                            contentRating.setName(rs.getString("name"));
                            return contentRating;
                        }, id)
                .stream()
                .findFirst();
    }
}
