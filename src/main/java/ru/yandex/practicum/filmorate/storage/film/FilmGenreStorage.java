package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface FilmGenreStorage {
    void addGenreToFilm(Long filmId, List<Genre> genreId);
}