package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findFilm(@PathVariable Long id) {
        return filmService.findFilm(id);
    }

    @GetMapping("/popular")
    public Collection<Film> getTopFilms(
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "The number of films must be greater than zero.") final Long count
    ) {
        return filmService.getTopFilms(count);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        return filmService.updateFilm(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeToFilm(@PathVariable Long id, @PathVariable Long userId) {
        if (userId == null || id == null) throw new ValidationException("IDs must not be null");
        filmService.addLikeToFilm(userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeToFilm(@PathVariable Long id, @PathVariable Long userId) {
        if (userId == null || id == null) throw new ValidationException("IDs must not be null");
        filmService.removeLikeToFilm(userId, id);
    }

    @DeleteMapping("/{id}")
    public void removeFilm(@PathVariable Long id) {
        if (id == null) throw new ValidationException("IDs must not be null");
        filmService.removeFilm(id);
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam Long userId, @RequestParam Long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getDirectorSortedFilm(@PathVariable Long directorId,
                                                  @RequestParam(name = "sortBy", defaultValue = "year") String sortType) {
        return filmService.getDirectorSortedFilms(directorId, sortType);
    }
}
