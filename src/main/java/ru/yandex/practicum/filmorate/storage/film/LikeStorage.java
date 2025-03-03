package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface LikeStorage {
    void addLikeToFilm(Long userId, Long filmId);

    void removeLikeToFilm(Long userId, Long filmId);

    void getFilmsLikes(List<Film> films);
}
