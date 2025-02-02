package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    private Film getFilmOrThrow(Long filmId) {
        return filmStorage.findFilm(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id = " + filmId + " not found"));
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findUser(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));
    }

    public void addLikeToFilm(Long userId, Long filmId) {
        log.info("User with ID {} is attempting to like film with ID {}", userId, filmId);
        User user = getUserOrThrow(userId);
        Film film = getFilmOrThrow(filmId);

        if (film.getLikes().contains(userId)) throw new ValidationException("User has already liked this film");

        film.addLike(userId);
        log.info("User {} liked the film: {}", user.getLogin(), film.getName());
    }

    public void removeLikeToFilm(Long userId, Long filmId) {
        log.info("User with ID {} is attempting to remove like from film with ID {}", userId, filmId);
        User user = getUserOrThrow(userId);
        Film film = getFilmOrThrow(filmId);

        if (film.getLikes() == null || film.getLikes().isEmpty())
            throw new ValidationException("This film has no likes yet");

        if (!film.getLikes().contains(userId)) throw new ValidationException("User has not liked this film");

        film.removeLike(userId);
        log.info("User {} remove like from the film: {}", user.getLogin(), film.getName());
    }

    public Collection<Film> getTopFilms(Long count) {
        List<Film> topFilms = filmStorage.findAll()
                .stream()
                .sorted((film1, film2) -> Long.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(count)
                .toList();
        log.info("Top films list {}", topFilms);
        return topFilms;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findFilm(Long id) {
        return getFilmOrThrow(id);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film updateFilm(Film newFilm) {
        return filmStorage.updateFilm(newFilm);
    }
}