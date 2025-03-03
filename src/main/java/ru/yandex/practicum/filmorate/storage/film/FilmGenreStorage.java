package ru.yandex.practicum.filmorate.storage.film;

public interface FilmGenreStorage {
    void addGenreToFilm(Long filmId, Integer genreId);
}