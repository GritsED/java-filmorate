package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.film.FilmDirectorStorage;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDirectorDbStorage implements FilmDirectorStorage {
    private static final String INSERT_FILM_DIRECTOR_QUERY = """
            MERGE INTO filmDirector (film_id, director_id)
            VALUES (?, ?)
            """;

    private final JdbcTemplate jdbc;

    @Override
    public void addDirectorToFilm(Long filmId, List<Director> directorId) {
        log.debug("Received request to add {} directors to film with ID {}", directorId.size(), filmId);
        jdbc.batchUpdate(INSERT_FILM_DIRECTOR_QUERY, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setLong(2, directorId.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return directorId.size();
            }
        });
        log.debug("Successfully added {} directors to film with ID {}", directorId.size(), filmId);
    }
}
