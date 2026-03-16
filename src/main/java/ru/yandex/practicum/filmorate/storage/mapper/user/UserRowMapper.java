package ru.yandex.practicum.filmorate.storage.mapper.user;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new User()
                .setId(rs.getLong("id"))
                .setName(rs.getString("name"))
                .setEmail(rs.getString("email"))
                .setLogin(rs.getString("login"))
                .setBirthday(rs.getObject("birthday", LocalDate.class));
    }
}
