package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getUsers();

    User addUser(User user);

    Optional<User> getUserById(Long id);

    void updateUser(User user);

    void clear();
}
