package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Optional<Film> findFilm(Long id);

    Film create(Film film);

    Film updateFilm(Film newFilm);

    void removeFilm(Long id);

    Collection<Film> getTopFilms(Long count);

    List<Film> getCommonFilms(Long userId, Long filmId);
}