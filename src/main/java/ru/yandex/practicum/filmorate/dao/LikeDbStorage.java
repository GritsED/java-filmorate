package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Optional;

@Repository
@Slf4j
public class LikeDbStorage implements LikeStorage {
    private static final String ADD_LIKE = """
            MERGE INTO likes(film_id, user_id)
            VALUES (?, ?)
            """;
    private static final String DELETE_LIKE = """
            DELETE FROM likes
            WHERE film_id = ? AND user_id = ?
            """;
    private final JdbcTemplate jdbc;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventDbStorage eventDbStorage;

    public LikeDbStorage(JdbcTemplate jdbc,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage,
                         EventDbStorage eventDbStorage) {
        this.jdbc = jdbc;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.eventDbStorage = eventDbStorage;

    }

    @Override
    public void addLikeToFilm(Long userId, Long filmId) {
        log.debug("Received request to add like. User ID: {}, Film ID: {}", userId, filmId);
        log.info("User with ID {} is attempting to like film with ID {}", userId, filmId);
        User user = userStorage.findUser(userId);
        Optional<Film> film = filmStorage.findFilm(filmId);

        if (film.isPresent()) {
            if (film.get().getLikes().contains(userId)) {
                log.info("User {} has already liked this film", user.getName());
            }

            film.get().addLike(userId);
            jdbc.update(ADD_LIKE, filmId, userId);
            log.info("User {} liked the film: {}", user.getLogin(), film.get().getName());
            eventDbStorage.add(filmId, userId, EventType.LIKE, Operation.ADD);
        }
    }

    @Override
    public void removeLikeToFilm(Long userId, Long filmId) {
        log.debug("Received request to remove like. User ID: {}, Film ID: {}", userId, filmId);

        log.info("User with ID {} is attempting to remove like from film with ID {}", userId, filmId);
        User user = userStorage.findUser(userId);
        Optional<Film> film = filmStorage.findFilm(filmId);

        if (film.isPresent()) {
            film.get().removeLike(userId);
            jdbc.update(DELETE_LIKE, filmId, userId);
            log.info("User {} remove like from the film: {}", user.getLogin(), film.get().getName());
            eventDbStorage.add(filmId, userId, EventType.LIKE, Operation.REMOVE);
        }
    }
}
