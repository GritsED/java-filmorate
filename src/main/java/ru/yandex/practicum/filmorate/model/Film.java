package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Builder
public class Film {
    Long id;
    @NotNull
    @NotBlank
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
}
