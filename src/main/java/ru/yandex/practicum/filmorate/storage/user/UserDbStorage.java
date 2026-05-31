package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.user.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository(value = "userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public Collection<User> getUsers() {
        return jdbcTemplate.query("SELECT * FROM users ORDER BY id", userRowMapper);
    }

    @Override
    public Collection<User> getUsers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = """
                SELECT id, name, email, birthday, login
                FROM users
                WHERE id IN (:userIds)
                ORDER BY id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("userIds", userIds);

        return namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setBirthday(rs.getObject("birthday", LocalDate.class));
            user.setLogin(rs.getString("login"));
            return user;
        });
    }

    @Override
    public User addUser(User user) {
        String sql = "INSERT INTO users(name, email, login, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getLogin());
            ps.setObject(4, user.getBirthday(), Types.DATE);
            return ps;
        }, keyHolder);

        return user.setId(keyHolder.getKey().longValue());
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return jdbcTemplate.query("SELECT * FROM users WHERE id = ?", userRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public void updateUser(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, user.getName(), user.getEmail(), user.getLogin(), user.getBirthday(), user.getId());
        if (updated == 0) {
            throw new UserNotFoundException(String.format("User with id %s not found", user.getId()));
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbcTemplate.update(
                "INSERT INTO friends (user_id, friend_id) " +
                        "SELECT ?, ? WHERE NOT EXISTS (" +
                        "SELECT 1 FROM friends WHERE user_id = ? AND friend_id = ?)",
                userId,
                friendId,
                userId,
                friendId
        );
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        jdbcTemplate.update(
                "DELETE FROM friends WHERE user_id = ? AND friend_id = ?",
                userId,
                friendId
        );
    }

    @Override
    public List<User> getUserFriends(Long userId) {
        return jdbcTemplate.query(
                "SELECT u.id, u.name, u.email, u.login, u.birthday " +
                        "FROM friends f " +
                        "JOIN users u ON u.id = f.friend_id " +
                        "WHERE f.user_id = ? " +
                        "ORDER BY u.id",
                userRowMapper,
                userId
        );
    }

    @Override
    public void clear() {
        jdbcTemplate.update("DELETE FROM users");
    }
}
