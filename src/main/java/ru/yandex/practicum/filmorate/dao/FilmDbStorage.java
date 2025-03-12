package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmDirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    public static final String GET_COMMON_FILMS = """
            SELECT f.*, m.id AS mpa_id, m.rate, COUNT(l.user_id) AS likes_count, g.id genre
            FROM films f
            JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN likes l ON f.id = l.film_id
            LEFT JOIN filmGenre fg ON fg.film_id = f.id
            LEFT JOIN genres g ON g.id = fg.genre_id
            WHERE f.id IN (
                SELECT film_id
                FROM likes
                WHERE user_id IN (?, ?)
                GROUP BY film_id
                HAVING COUNT(DISTINCT user_id) = 2
            )
            GROUP BY f.id, m.id, m.rate, g.id
            ORDER BY likes_count DESC
            """;
    public static final String GET_DIRECTOR_FILMS_SORTED_BY_YEAR = """
            SELECT f.*, m.id AS mpa_id, m.rate
            FROM films f
            JOIN mpa m ON f.mpa_id = m.id
            JOIN filmDirector fd ON f.id = fd.film_id
            WHERE fd.director_id = ?
            ORDER BY f.releaseDate ASC""";
    public static final String GET_DIRECTOR_FILMS_SORTED_BY_LIKES = """
            SELECT f.*, m.id AS mpa_id, m.rate, COUNT(l.user_id) AS like_count
            FROM films f
            JOIN mpa m ON f.mpa_id = m.id
            JOIN filmDirector fd ON f.id = fd.film_id
            LEFT JOIN likes l ON f.id = l.film_id
            WHERE fd.director_id = ?
            GROUP BY f.id
            ORDER BY like_count DESC""";
    private static final String GET_ALL_FILMS = """
            SELECT f.*, m.id AS mpa_id, m.rate AS mpa_name
            FROM films f
            JOIN mpa m ON m.id = f.mpa_id
            """;
    private static final String GET_FILM_BY_ID = """
            SELECT *
            FROM films f
            JOIN mpa m ON m.id = f.mpa_id
            WHERE f.id = ?
            """;
    private static final String INSERT_FILM = """
            INSERT INTO films(name, description, releaseDate, duration, mpa_id)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_FILM = """
            UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ?, mpa_id = ?
            WHERE id = ?
            """;
    private static final String DELETE_FILM = """
            DELETE FROM films
            WHERE id = ?
            """;
    private static final String GET_TOP_FILMS = """
            SELECT f.*, m.id AS mpa_id, m.rate, COUNT(l.user_id) AS likes_count, g.id genre
            FROM films f
            JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN likes l ON f.id = l.film_id
            LEFT JOIN filmGenre fg ON fg.film_id = f.id
            LEFT JOIN genres g ON g.id = fg.genre_id
            GROUP BY f.id, m.id, m.rate, g.id
            ORDER BY likes_count DESC
            LIMIT ?
            """;
    private static final String GET_LIKES = """
            SELECT user_id
            FROM likes
            WHERE film_id = ?
            """;
    private static final String GET_FILM_LIKES = """
            SELECT *
            FROM likes
            WHERE film_id IN (:filmIds)
            """;
    private static final String GET_GENRE = """
            SELECT g.id, g.name
            FROM filmGenre fg
            JOIN genres g ON g.id = fg.genre_id
            WHERE fg.film_id = ?
            """;
    private static final String GET_FILM_GENRES = """
            SELECT *
            FROM filmGenre fg
            JOIN genres g ON fg.genre_id = g.id
            WHERE film_id IN (:filmIds)
            """;
    private static final String GET_FILMS_BY_TITLE = """
            SELECT f.* , m.rate, COUNT(l.user_id) likes
            FROM films f
            JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN likes l ON f.id = l.film_id
            WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%'))
            GROUP BY f.id
            ORDER BY likes DESC
            """;
    private static final String GET_FILMS_BY_DIRECTOR = """
            SELECT f.* , m.rate, d.name directors, COUNT(l.user_id) likes
            FROM films f
            JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN likes l ON f.id = l.film_id
            LEFT JOIN filmDirector fd ON fd.film_id = f.id
            LEFT JOIN directors d ON fd.director_id = d.id
            WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', ?, '%'))
            GROUP BY f.id
            ORDER BY likes DESC
            """;
    private static final String GET_FILMS_BY_TITLE_AND_DIRECTOR = """
            SELECT f.* , m.rate, d.name directors, COUNT(l.user_id) likes
            FROM films f
            JOIN mpa m ON f.mpa_id = m.id
            LEFT JOIN likes l ON f.id = l.film_id
            LEFT JOIN filmDirector fd ON fd.film_id = f.id
            LEFT JOIN directors d ON fd.director_id = d.id
            WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', ?, '%')) OR LOWER(d.name) LIKE LOWER(CONCAT('%', ?, '%'))
            GROUP BY f.id
            ORDER BY likes DESC
            """;
    private static final String GET_DIRECTOR = """
            SELECT d.id, d.name
            FROM filmDirector fd
            JOIN directors d ON fd.film_id = d.id
            WHERE fd.film_id = ?
            """;
    private static final String GET_FILM_DIRECTORS = """
            SELECT *
            FROM filmDirector fd
            JOIN directors d ON fd.director_id = d.id
            WHERE film_id IN (:directorIds)
            """;
    private static final String GET_RECOMMENDATIONS = """
            SELECT f.*, m.id AS mpa_id, m.rate
            FROM films f
            JOIN mpa m ON f.mpa_id = m.id
            JOIN (
                SELECT l.film_id
                FROM likes l
                WHERE l.user_id = (
                    SELECT l2.user_id
                    FROM likes l1
                    JOIN likes l2 ON l1.film_id = l2.film_id
                    WHERE l1.user_id = :user_id
                      AND l2.user_id != :user_id
                    GROUP BY l2.user_id
                    ORDER BY COUNT(DISTINCT l2.film_id) DESC
                    LIMIT 1
                )
                AND l.film_id NOT IN (
                    SELECT film_id
                    FROM likes
                    WHERE user_id = :user_id
                )
            ) recommended_films ON f.id = recommended_films.film_id
            """;

    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate namedJdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final FilmGenreStorage filmGenreStorage;
    private final DirectorRowMapper directorRowMapper;
    private final FilmDirectorStorage filmDirectorStorage;

    @Override
    public Collection<Film> findAll() {
        log.debug("Received request to retrieve all films");
        List<Film> films = jdbc.query(GET_ALL_FILMS, filmRowMapper);
        getFilmsLikes(films);
        getFilmsGenres(films);
        getFilmsDirectors(films);
        log.debug("Returning list of films");
        return films;
    }

    @Override
    public Optional<Film> findFilm(Long id) {
        log.debug("Received request to find film with ID {}", id);
        try {
            Film film = jdbc.queryForObject(GET_FILM_BY_ID, filmRowMapper, id);
            Set<Long> likes = new HashSet<>(jdbc.query(GET_LIKES,
                    (rs, rowNum) -> rs.getLong("user_id"), id));
            film.setLikes(likes);
            Set<Genre> genres = new LinkedHashSet<>(jdbc.query(GET_GENRE, genreRowMapper, film.getId()));
            film.setGenres(genres);
            Set<Director> directors = new LinkedHashSet<>(jdbc.query(GET_DIRECTOR, directorRowMapper, film.getId()));
            film.setDirectors(directors);

            log.debug("Returning film details for ID {}", id);
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Film with ID {} not found", id);
            throw new NotFoundException("Film with id " + id + " not found");
        }
    }

    @Override
    public Film create(Film film) {
        log.debug("Received request to create a new film: {}", film);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(id);
        film.getGenres().forEach(genre -> filmGenreStorage.addGenreToFilm(film.getId(), genre.getId()));
        film.getDirectors().forEach(director -> filmDirectorStorage.addDirectorToFilm(film.getId(), director.getId()));
        log.debug("Film successfully added with ID {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        log.debug("Received request to update film with ID: {}", newFilm.getId());

        if (newFilm.getId() == null) {
            log.warn("Failed to update film: ID is missing");
            throw new IllegalArgumentException("ID must be specified.");
        }
        int update = jdbc.update(
                UPDATE_FILM,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getId()
        );
        if (update == 0) {
            log.warn("Film update failed: Film with ID {} not found", newFilm.getId());
            throw new NotFoundException("Film with id  = " + newFilm.getId() + " not found.");
        }
        newFilm.getGenres().forEach(genre -> filmGenreStorage.addGenreToFilm(newFilm.getId(), genre.getId()));
        newFilm.getDirectors().forEach(director -> filmDirectorStorage.addDirectorToFilm(newFilm.getId(), director.getId()));
        log.debug("Film with ID {} successfully updated", newFilm.getId());
        return newFilm;
    }

    @Override
    public void removeFilm(Long id) {
        log.debug("Received request to remove film with ID: {}", id);

        int deletedRows = jdbc.update(DELETE_FILM, id);
        if (deletedRows == 0) {
            log.debug("Film with ID {} not found", id);
            throw new NotFoundException("Film with id  = " + id + " not found.");
        }
    }

    @Override
    public Collection<Film> getTopFilms(Long count) {
        log.debug("Received request to get top {} films", count);
        List<Film> films = jdbc.query(GET_TOP_FILMS, filmRowMapper, count);
        log.debug("Returning top films list with {} entries", films.size());
        getFilmsLikes(films);
        getFilmsGenres(films);
        getFilmsDirectors(films);
        log.debug("Returning list of top films");
        return films;
    }

    @Override
    public Collection<Film> getFilmsByTitle(String query) {
        List<Film> films = jdbc.query(GET_FILMS_BY_TITLE, filmRowMapper, query);
        getFilmsLikes(films);
        getFilmsGenres(films);
        getFilmsDirectors(films);
        return films;
    }

    @Override
    public Collection<Film> getFilmsByDirector(String query) {
        List<Film> films = jdbc.query(GET_FILMS_BY_DIRECTOR, filmRowMapper, query);
        getFilmsLikes(films);
        getFilmsGenres(films);
        getFilmsDirectors(films);
        return films;
    }

    @Override
    public Collection<Film> getFilmsByTitleAndDirector(String query) {
        List<Film> films = jdbc.query(GET_FILMS_BY_TITLE_AND_DIRECTOR, filmRowMapper, query, query);
        getFilmsLikes(films);
        getFilmsGenres(films);
        getFilmsDirectors(films);
        return films;
    }

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        log.debug("Received request to get common films between user with ID: {} and user with ID: {}",
                userId, friendId);
        List<Film> films = jdbc.query(GET_COMMON_FILMS, filmRowMapper, userId, friendId);
        getFilmsGenres(films);
        getFilmsDirectors(films);
        log.debug("Returning list of common films");
        return films;
    }

    @Override
    public Collection<Film> getDirectorSortedFilms(Long directorId, String sortType) {
        List<Film> films;
        switch (sortType.toLowerCase()) {
            case "year":
                log.debug("A request to receive films by the director with the ID: {}, sorted by year.", directorId);
                films = jdbc.query(GET_DIRECTOR_FILMS_SORTED_BY_YEAR, filmRowMapper, directorId);
                break;
            case "likes":
                log.debug("A request to receive films by the director with the ID: {}, sorted by likes.", directorId);
                films = jdbc.query(GET_DIRECTOR_FILMS_SORTED_BY_LIKES, filmRowMapper, directorId);
                break;
            default:
                throw new IllegalArgumentException("Acceptable values for sortBy are \"year\" and \"likes\".");
        }
        getFilmsLikes(films);
        getFilmsGenres(films);
        getFilmsDirectors(films);
        log.debug("Returning list of sorted films");
        return films;
    }

    @Override
    public Collection<Film> getRecommendations(Long id) {
        log.debug("Received request to retrieve recommendations");
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", id);

        log.debug("Returning list of films");
        return namedJdbc.query(GET_RECOMMENDATIONS, params, filmRowMapper);
    }

    private void getFilmsDirectors(List<Film> films) {
        List<Long> filmsId = films.stream().map(Film::getId).toList();
        Map<String, Object> params = new HashMap<>();
        params.put("directorIds", filmsId);
        Map<Long, Set<Director>> filmDirectorMap = namedJdbc.query(GET_FILM_DIRECTORS, params,
                rs -> {
                    Map<Long, Set<Director>> map = new HashMap<>();
                    while (rs.next()) {
                        Long filmId = rs.getLong("film_id");
                        Director directorId = new Director(rs.getLong("director_id"), rs.getString("name"));
                        map.computeIfAbsent(filmId, v -> new LinkedHashSet<>()).add(directorId);
                    }
                    return map;
                });
        for (Film film : films) {
            Set<Director> directors = filmDirectorMap.get(film.getId());
            film.setDirectors(Objects.requireNonNullElseGet(directors, LinkedHashSet::new));
        }
    }

    private void getFilmsGenres(List<Film> films) {
        List<Long> filmsId = films.stream().map(Film::getId).toList();
        Map<String, Object> params = new HashMap<>();
        params.put("filmIds", filmsId);
        Map<Long, Set<Genre>> filmGenresMap = namedJdbc.query(GET_FILM_GENRES, params,
                rs -> {
                    Map<Long, Set<Genre>> map = new HashMap<>();
                    while (rs.next()) {
                        Long filmId = rs.getLong("film_id");
                        Genre genreId = new Genre(rs.getInt("genre_id"), rs.getString("name"));
                        map.computeIfAbsent(filmId, v -> new LinkedHashSet<>()).add(genreId);
                    }
                    return map;
                });
        for (Film film : films) {
            Set<Genre> genres = filmGenresMap.get(film.getId());
            film.setGenres(Objects.requireNonNullElseGet(genres, LinkedHashSet::new));
        }
    }

    private void getFilmsLikes(List<Film> films) {
        List<Long> filmsId = films.stream().map(Film::getId).toList();
        Map<String, Object> params = new HashMap<>();
        params.put("filmIds", filmsId);

        Map<Long, Set<Long>> filmLikesMap = namedJdbc.query(GET_FILM_LIKES, params,
                rs -> {
                    Map<Long, Set<Long>> map = new HashMap<>();
                    while (rs.next()) {
                        Long filmId = rs.getLong("film_id");
                        Long userId = rs.getLong("user_id");
                        map.computeIfAbsent(filmId, v -> new HashSet<>()).add(userId);
                    }
                    return map;
                });
        for (Film film : films) {
            Set<Long> likes = filmLikesMap.get(film.getId());
            film.setLikes(Objects.requireNonNullElseGet(likes, HashSet::new));
        }
    }
}