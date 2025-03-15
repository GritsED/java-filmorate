package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmGenreStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmGenreDbStorage implements FilmGenreStorage {
    private static final String INSERT_FILM_GENRE_QUERY = """
            MERGE INTO filmGenre (film_id, genre_id)
            VALUES (?, ?)
            """;

    private final JdbcTemplate jdbc;

    @Override
    public void addGenreToFilm(Long filmId, List<Genre> genreIds) {
        log.debug("Received request to add {} genres to film with ID {}", genreIds.size(), filmId);
        jdbc.batchUpdate(INSERT_FILM_GENRE_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setInt(2, genreIds.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genreIds.size();
            }
        });
        log.debug("Successfully added {} genres to film with ID {}", genreIds.size(), filmId);
    }
}
