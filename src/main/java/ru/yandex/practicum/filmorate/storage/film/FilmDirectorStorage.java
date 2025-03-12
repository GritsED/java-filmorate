package ru.yandex.practicum.filmorate.storage.film;

public interface FilmDirectorStorage {
    void addDirectorToFilm(Long filmId, Long directorId);
}
