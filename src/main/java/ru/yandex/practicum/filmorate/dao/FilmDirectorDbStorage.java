package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.film.FilmDirectorStorage;

@Repository
@RequiredArgsConstructor
public class FilmDirectorDbStorage implements FilmDirectorStorage {
    private static final String INSERT_FILM_DIRECTOR_QUERY = """
            INSERT INTO filmDirector (film_id, director_id)
            VALUES (?, ?)
            """;

    private final JdbcTemplate jdbc;

    @Override
    public void addDirectorToFilm(Long filmId, Long directorId) {
        jdbc.update(INSERT_FILM_DIRECTOR_QUERY, filmId, directorId);
    }
}
