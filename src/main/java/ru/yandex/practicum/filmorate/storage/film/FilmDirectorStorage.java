package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface FilmDirectorStorage {
    void addDirectorToFilm(Long filmId, List<Director> directorId);
}
