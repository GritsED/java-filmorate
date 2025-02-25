package ru.yandex.practicum.filmorate.storage.film;

public interface LikeStorage {
    void addLikeToFilm(Long userId, Long filmId);

    void removeLikeToFilm(Long userId, Long filmId);
}
