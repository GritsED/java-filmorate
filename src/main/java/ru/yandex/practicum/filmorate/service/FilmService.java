package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;
import ru.yandex.practicum.filmorate.storage.film.LikeStorage;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final LikeStorage likeStorage;

    private Film getFilmOrThrow(Long filmId) {
        return filmStorage.findFilm(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id = " + filmId + " not found"));
    }

    public void addLikeToFilm(Long userId, Long filmId) {
        likeStorage.addLikeToFilm(userId, filmId);
    }

    public void removeLikeToFilm(Long userId, Long filmId) {
        likeStorage.removeLikeToFilm(userId, filmId);
    }

    public Collection<Film> getTopFilms(Long count, Integer genreId, Integer year) {
        return filmStorage.getTopFilms(count, genreId, year);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findFilm(Long id) {
        return getFilmOrThrow(id);
    }

    public Film create(Film film) {
        Integer mpaId = film.getMpa().getId();
        if (mpaStorage.findMpa(mpaId) == null) {
            throw new NotFoundException("MPA with id " + mpaId + " not found");
        }

        for (Genre genre : film.getGenres()) {
            Integer genreId = genre.getId();
            if (genreStorage.findGenre(genreId) == null) {
                throw new NotFoundException("Genre with id " + genreId + " not found");
            }
        }
        return filmStorage.create(film);
    }

    public Film updateFilm(Film newFilm) {
        return filmStorage.updateFilm(newFilm);
    }

    public void removeFilm(Long id) {
        filmStorage.removeFilm(id);
    }

    public Collection<Film> searchFilm(String query, Set<String> by) {
        if (query == null && (by == null || by.isEmpty())) {
            return filmStorage.getTopFilms(10L, null, null);
        }
        if (by.contains("director") && by.contains("title")) {
            return filmStorage.getFilmsByTitleAndDirector(query);
        } else if (by.contains("title")) {
            return filmStorage.getFilmsByTitle(query);
        } else if (by.contains("director")) {
            return filmStorage.getFilmsByDirector(query);
        }
        return Collections.emptyList();
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public Collection<Film> getDirectorSortedFilms(Long directorId, String sortType) {
        return filmStorage.getDirectorSortedFilms(directorId, sortType);
    }

    public Collection<Film> getRecommendations(Long id) {
        return filmStorage.getRecommendations(id);
    }
}