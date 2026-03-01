package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final List<User> users = new ArrayList<>();
    private long nextId = 1;

    public List<User> getUsers() {
        return users;
    }

    public User addUser(User user) {
        log.info("Adding user: {}", user.getLogin());
        if (user.getId() == null) {
            user.setId(nextId++);
        }
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        users.add(user);
        return user;
    }

    public User updateUser(User user) {
        log.info("Updating user: {} with id {}", user.getLogin(), user.getId());
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        Optional<User> foundUser = users.stream()
                .filter(u -> user.getId().equals(u.getId()))
                .findFirst();
        if (foundUser.isPresent()) {
            var updatedUser = foundUser.get();
            updatedUser.setEmail(user.getEmail());
            updatedUser.setLogin(user.getLogin());
            updatedUser.setName(user.getName());
            updatedUser.setBirthday(user.getBirthday());
            return updatedUser;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}
