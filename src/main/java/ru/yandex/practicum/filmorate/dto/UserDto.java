package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserDto {
    private Long id;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\S+$", message = "Login must not contain spaces")
    private String login;

    private String name;

    @PastOrPresent
    @NotNull
    private LocalDate birthday;
}

