package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class User {
    @NotNull
    Long id;
    @Email
    String email;
    @NotBlank
    String login;
    String name;
    @NotNull
    LocalDate birthday;
}
