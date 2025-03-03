package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.LikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Repository
@Slf4j
public class LikeDbStorage implements LikeStorage {
    private static final String ADD_LIKE = """
            INSERT INTO likes(film_id, user_id)
            VALUES (?, ?)
            """;
    private static final String DELETE_LIKE = """
            DELETE FROM likes
            WHERE film_id = ? AND user_id = ?
            """;
    private static final String GET_FILM_LIKES = """
            SELECT *
            FROM likes
            WHERE film_id IN (?)
            """;
    private final JdbcTemplate jdbc;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public LikeDbStorage(JdbcTemplate jdbc,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.jdbc = jdbc;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @Override
    public void addLikeToFilm(Long userId, Long filmId) {
        log.debug("Received request to add like. User ID: {}, Film ID: {}", userId, filmId);
        log.info("User with ID {} is attempting to like film with ID {}", userId, filmId);
        User user = userStorage.findUser(userId);
        Optional<Film> film = filmStorage.findFilm(filmId);

        if (film.isPresent()) {
            if (film.get().getLikes().contains(userId))
                throw new ValidationException("User has already liked this film");

            film.get().addLike(userId);
            jdbc.update(ADD_LIKE, filmId, userId);
            log.info("User {} liked the film: {}", user.getLogin(), film.get().getName());
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
        }
    }

    @Override
    public void getFilmsLikes(List<Film> films) {
        List<Long> filmsId = films.stream().map(Film::getId).toList();
        String placeholder = String.join(", ", Collections.nCopies(filmsId.size(), "?"));

        Map<Long, Set<Long>> filmLikesMap = jdbc.query(GET_FILM_LIKES.replace("?", placeholder),
                rs -> {
                    Map<Long, Set<Long>> map = new HashMap<>();
                    while (rs.next()) {
                        Long filmId = rs.getLong("film_id");
                        Long userId = rs.getLong("user_id");
                        map.computeIfAbsent(filmId, v -> new HashSet<>()).add(userId);
                    }
                    return map;
                },
                filmsId.toArray());
        for (Film film : films) {
            Set<Long> likes = filmLikesMap.get(film.getId());
            film.setLikes(Objects.requireNonNullElseGet(likes, HashSet::new));
        }
    }
}
