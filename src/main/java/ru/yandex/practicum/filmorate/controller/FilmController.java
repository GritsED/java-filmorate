package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        if (isValid(film)) {
            film.setId(getNextId());
            films.put(film.getId(), film);
            log.info("Film added: {}", film);
        }
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            String msg = "ID must be specified.";
            log.error(msg);
            throw new ValidationException(msg);
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            if (isValid(newFilm)) {
                oldFilm.setName(newFilm.getName());
                oldFilm.setDescription(newFilm.getDescription());
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
                oldFilm.setDuration(newFilm.getDuration());

                log.info("Film with id = {} has been updated", newFilm.getId());
            }
            return oldFilm;
        }
        String msg = "Film with id = " + newFilm.getId() + " not found.";
        log.error(msg);
        throw new ValidationException(msg);
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean isValid(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            String msg = "Name cannot be empty.";
            log.error(msg);
            throw new ValidationException(msg);
        }

        int MAX_SIMBOL_DISCRIPTION = 200;
        if (film.getDescription().length() >= MAX_SIMBOL_DISCRIPTION) {
            String msg = "Description cannot exceed 200 characters.";
            log.error(msg);
            throw new ValidationException(msg);
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12,27))) {
            String msg = "Cinema's birthday is December 28, 1895.";
            log.error(msg);
            throw new ValidationException(msg);
        }

        if (film.getDuration() < 0) {
            String msg = "Movie duration must be greater than zero.";
            log.error(msg);
            throw new ValidationException(msg);
        }
        return true;
    }
}
