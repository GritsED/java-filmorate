package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.service.ReleaseDate;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@Builder
public class Film {
    Long id;
    @NotEmpty(message = "Name cannot be empty.")
    String name;
    @Size(max = 200, message = "Description cannot exceed 200 characters.")
    String description;
    @ReleaseDate(message = "Cinema's birthday is December 28, 1895.")
    LocalDate releaseDate;
    @Min(value = 0, message = "Movie duration must be greater than zero.")
    int duration;
}
