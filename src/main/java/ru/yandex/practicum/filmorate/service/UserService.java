package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    public List<UserDto> getUsers() {
        log.info("Fetching all users");
        return userStorage.getUsers().stream()
                .map(userMapper::mapToDto)
                .toList();
    }

    public UserDto addUser(UserDto userDto) {
        User user = userMapper.map(userDto);
        log.info("Adding user: {}", user.getLogin());
        normalizeUserName(user);
        userStorage.addUser(user);
        return userMapper.mapToDto(user);
    }

    public UserDto updateUser(UserDto userDto) {
        User user = userMapper.map(userDto);
        log.info("Updating user: {} with id {}", user.getLogin(), user.getId());
        normalizeUserName(user);
        User updatedUser = getRequiredUser(user.getId());

        updatedUser.setEmail(user.getEmail());
        updatedUser.setLogin(user.getLogin());
        updatedUser.setName(user.getName());
        updatedUser.setBirthday(user.getBirthday());
        userStorage.updateUser(updatedUser);

        return userMapper.mapToDto(updatedUser);
    }

    public void clearUsers() {
        log.info("Clearing all users");
        userStorage.clear();
    }

    public UserDto getUser(Long id) {
        log.info("Fetching user with id {}", id);
        return userMapper.mapToDto(getRequiredUser(id));
    }

    public void addFriend(Long id, Long friendId) {
        log.info("Adding friend: user {} -> friend {}", id, friendId);
        validateDifferentUserIds(id, friendId);
        User user = getRequiredUser(id);
        getRequiredUser(friendId);
        user.getFriends().add(friendId);
        userStorage.updateUser(user);
    }

    public void deleteFriend(Long id, Long friendId) {
        log.info("Deleting friend: user {} -> friend {}", id, friendId);
        validateDifferentUserIds(id, friendId);
        User user = getRequiredUser(id);
        getRequiredUser(friendId);
        user.getFriends().remove(friendId);

        userStorage.updateUser(user);
    }

    public List<UserDto> getFriends(Long id) {
        log.info("Fetching friends for user {}", id);
        User user = getRequiredUser(id);
        return user.getFriends().stream()
                .sorted()
                .map(userStorage::getUserById)
                .flatMap(Optional::stream)
                .map(userMapper::mapToDto)
                .toList();
    }

    private User getRequiredUser(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with id %s not found", id)));
    }

    public List<UserDto> getCommonFriends(Long id, Long otherId) {
        log.info("Fetching common friends for users {} and {}", id, otherId);
        validateDifferentUserIds(id, otherId);
        User firstUser = getRequiredUser(id);
        User secondUser = getRequiredUser(otherId);
        Set<Long> commonFriendIds = new HashSet<>(firstUser.getFriends());
        commonFriendIds.retainAll(secondUser.getFriends());
        return commonFriendIds.stream()
                .sorted()
                .map(this::getRequiredUser)
                .map(userMapper::mapToDto)
                .toList();
    }

    private void validateDifferentUserIds(Long firstId, Long secondId) {
        if (firstId != null && firstId.equals(secondId)) {
            throw new ValidationException("User id and friend id must be different");
        }
    }

    private void normalizeUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
