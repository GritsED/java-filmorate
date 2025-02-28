package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.film.FilmGenreStorage;

@Repository
@RequiredArgsConstructor
public class FilmGenreDbStorage implements FilmGenreStorage {
    private static final String INSERT_FILM_GENRE_QUERY = """
            INSERT INTO filmGenre (film_id, genre_id)
            VALUES (?, ?)
            """;

    private final JdbcTemplate jdbc;

    @Override
    public void addGenreToFilm(Long filmId, Integer genreId) {
        jdbc.update(INSERT_FILM_GENRE_QUERY, filmId, genreId);
    }
}
