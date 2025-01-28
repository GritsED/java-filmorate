package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    private Optional<Film> findFilm(Long filmId) {
        return filmStorage.findAll()
                .stream()
                .filter(film -> Objects.equals(film.getId(), filmId))
                .findFirst();
    }

    public void addLikeToFilm(Long userId, Long filmId) {
        if (userId == null || filmId == null) throw new ValidationException("IDs must not be null");

        log.info("User with ID {} is attempting to like film with ID {}", userId, filmId);
        User user = userService.findUser(userId)
                .orElseThrow(() -> new ValidationException("User with id = " + userId + " not found"));
        Film film = findFilm(filmId)
                .orElseThrow(() -> new ValidationException("Film with id = " + filmId + " not found"));

        if (film.getLikes().contains(userId)) throw new ValidationException("User has already liked this film");

        film.getLikes().add(userId);
        log.info("User {} liked the film: {}", user.getLogin(), film.getName());
    }

    public void removeLikeToFilm(Long userId, Long filmId) {
        if (userId == null || filmId == null) throw new ValidationException("IDs must not be null");

        log.info("User with ID {} is attempting to remove like from film with ID {}", userId, filmId);
        User user = userService.findUser(userId)
                .orElseThrow(() -> new ValidationException("User with id = " + userId + " not found"));
        Film film = findFilm(filmId)
                .orElseThrow(() -> new ValidationException("Film with id = " + filmId + " not found"));

        if (film.getLikes() == null || film.getLikes().isEmpty())
            throw new ValidationException("This film has no likes yet");

        if (!film.getLikes().contains(userId)) throw new ValidationException("User has not liked this film");

        film.getLikes().remove(userId);
        log.info("User {} remove like from the film: {}", user.getLogin(), film.getName());
    }

    public Collection<Film> getTopFilms() {
        return filmStorage.findAll()
                .stream()
                .sorted((film1, film2) -> Long.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(10)
                .toList();
    }
}