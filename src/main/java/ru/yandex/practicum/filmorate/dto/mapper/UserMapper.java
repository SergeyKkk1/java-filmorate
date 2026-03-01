package ru.yandex.practicum.filmorate.dto.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

@Mapper
public interface UserMapper {
    UserDto mapToDto(User user);

    User map(UserDto userDto);
}

