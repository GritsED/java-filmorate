package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Optional<Film> findFilm(Long id);

    Film create(Film film);

    Film updateFilm(Film newFilm);

    void removeFilm(Long id);

    Collection<Film> getTopFilms(Long count, Integer genreId, Integer year);

    Collection<Film> getCommonFilms(Long userId, Long filmId);

    Collection<Film> getDirectorSortedFilms(Long directorId, String sortType);

    Collection<Film> getRecommendations(Long id);
}