package ru.yandex.practicum.filmorate.exception;

public class UserIdIsNullException extends RuntimeException {
    public UserIdIsNullException(String message) {
        super(message);
    }
}

