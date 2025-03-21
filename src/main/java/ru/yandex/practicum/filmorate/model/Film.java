package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.yandex.practicum.filmorate.validator.ReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Film {
    @EqualsAndHashCode.Include
    Long id;

    @NotEmpty(message = "Name cannot be empty.")
    String name;
    @Size(max = 200, message = "Description cannot exceed 200 characters.")
    String description;
    @ReleaseDate
    LocalDate releaseDate;
    @Min(value = 0, message = "Movie duration must be greater than zero.")
    int duration;
    Set<Long> likes = new HashSet<>();
    Mpa mpa;
    LinkedHashSet<Genre> genres = new LinkedHashSet<>();
    Set<Director> directors = new LinkedHashSet<>();

    public void addLike(Long userId) {
        if (likes == null) {
            likes = new HashSet<>();
        }
        likes.add(userId);
    }

    public void removeLike(Long userId) {
        likes.remove(userId);
    }
}