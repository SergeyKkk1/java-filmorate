package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.mapper.UserMapper;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final Map<Long, User> users = new LinkedHashMap<>();
    private final UserMapper userMapper;
    private long nextId = 1;

    public List<UserDto> getUsers() {
        return users.values().stream()
                .map(userMapper::mapToDto)
                .toList();
    }

    public UserDto addUser(UserDto userDto) {
        User user = userMapper.map(userDto);
        log.info("Adding user: {}", user.getLogin());
        user.setId(nextId++);
        normalizeUserName(user);
        users.put(user.getId(), user);
        return userMapper.mapToDto(user);
    }

    public UserDto updateUser(UserDto userDto) {
        User user = userMapper.map(userDto);
        log.info("Updating user: {} with id {}", user.getLogin(), user.getId());
        normalizeUserName(user);
        User updatedUser = users.get(user.getId());
        if (updatedUser == null) {
            throw new UserNotFoundException(String.format("User with id %s not found", user.getId()));
        }

        updatedUser.setEmail(user.getEmail());
        updatedUser.setLogin(user.getLogin());
        updatedUser.setName(user.getName());
        updatedUser.setBirthday(user.getBirthday());

        return userMapper.mapToDto(updatedUser);
    }

    public void clearUsers() {
        users.clear();
        nextId = 1;
    }

    private void normalizeUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
