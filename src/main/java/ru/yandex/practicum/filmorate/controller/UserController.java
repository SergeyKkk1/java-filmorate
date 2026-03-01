package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.dto.mapper.UserMapper;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getUsers().stream().map(userMapper::mapToDto).toList();
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto user) {
        return userMapper.mapToDto(userService.addUser(userMapper.map(user)));
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody UserDto user) {
        if (user.getId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User id is null");
        }
        return userMapper.mapToDto(userService.updateUser(userMapper.map(user)));
    }
}
