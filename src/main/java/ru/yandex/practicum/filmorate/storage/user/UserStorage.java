package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getUsers();

    Collection<User> getUsers(Collection<Long> userIds);

    User addUser(User user);

    Optional<User> getUserById(Long id);

    void updateUser(User user);

    void addFriend(Long userId, Long friendId);

    void deleteFriend(Long userId, Long friendId);

    List<User> getUserFriends(Long userId);

    void clear();
}
