package ru.yandex.practicum.filmorate.exception;

public class ContentRatingNotFoundException extends RuntimeException {
    public ContentRatingNotFoundException(String message) {
        super(message);
    }
}
