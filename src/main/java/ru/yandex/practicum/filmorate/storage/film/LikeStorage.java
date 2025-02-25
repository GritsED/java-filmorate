package ru.yandex.practicum.filmorate.storage.film;

import java.util.Set;

public interface LikeStorage {
    void addLikeToFilm(Long userId, Long filmId);

    void removeLikeToFilm(Long userId, Long filmId);
}
