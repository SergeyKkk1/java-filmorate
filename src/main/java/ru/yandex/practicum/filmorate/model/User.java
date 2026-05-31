package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User
 */
@Data
@Accessors(chain = true)
public class User {
    private Long id;
    private String name;
    private String email;
    private String login;
    private LocalDate birthday;
    private Set<Long> friends = new HashSet<>();
}
