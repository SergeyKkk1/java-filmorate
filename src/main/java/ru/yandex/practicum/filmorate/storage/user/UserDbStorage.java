package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.user.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@Repository(value = "userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public Collection<User> getUsers() {
        Collection<User> users = jdbcTemplate.query("SELECT * FROM users ORDER BY id", userRowMapper);
        users.forEach(this::fillUserFriends);
        return users;
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
        Optional<User> user = jdbcTemplate.query("SELECT * FROM users WHERE id = ?", userRowMapper, id)
                .stream()
                .findFirst();
        user.ifPresent(this::fillUserFriends);
        return user;
    }

    @Override
    public void updateUser(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, user.getName(), user.getEmail(), user.getLogin(), user.getBirthday(), user.getId());
        if (updated == 0) {
            throw new UserNotFoundException(String.format("User with id %s not found", user.getId()));
        }
        updateFriends(user);
    }

    @Override
    public void clear() {
        jdbcTemplate.update("DELETE FROM friends");
        jdbcTemplate.update("DELETE FROM users");
    }

    private void fillUserFriends(User user) {
        user.setFriends(new HashSet<>(jdbcTemplate.queryForList(
                "SELECT friend_id FROM friends WHERE user_id = ? ORDER BY friend_id",
                Long.class,
                user.getId()
        )));
    }

    private void updateFriends(User user) {
        jdbcTemplate.update("DELETE FROM friends WHERE user_id = ?", user.getId());
        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO friends(user_id, friend_id) VALUES (?, ?)",
                user.getFriends(),
                user.getFriends().size(),
                (ps, friendId) -> {
                    ps.setLong(1, user.getId());
                    ps.setLong(2, friendId);
                }
        );
    }
}
