package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.UserIdIsNullException;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto user) {
        return userService.addUser(user);
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody UserDto user) {
        if (user.getId() == null) {
            throw new UserIdIsNullException("User id is null");
        }
        return userService.updateUser(user);
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        if (id == null) {
            throw new UserIdIsNullException("User id is null");
        }
        return userService.getUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if (id == null || friendId == null) {
            throw new UserIdIsNullException("User id is null");
        }
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if (id == null || friendId == null) {
            throw new UserIdIsNullException("User id is null");
        }
        userService.deleteFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDto> getFriends(@PathVariable Long id) {
        if (id == null) {
            throw new UserIdIsNullException("User id is null");
        }
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> getFriends(@PathVariable Long id,  @PathVariable Long otherId) {
        if (id == null || otherId == null) {
            throw new UserIdIsNullException("User id is null");
        }
        return userService.getCommonFriends(id, otherId);
    }
}
