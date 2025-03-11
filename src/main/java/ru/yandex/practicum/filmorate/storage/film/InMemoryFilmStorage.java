package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> findFilm(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Film added: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        if (newFilm.getId() == null) {
            String msg = "ID must be specified.";
            log.error(msg);
            throw new ValidationException(msg);
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());

            log.info("Film with id = {} has been updated - {}", newFilm.getId(), newFilm);
            return oldFilm;
        }
        String msg = "Film with id = " + newFilm.getId() + " not found.";
        log.error(msg);
        throw new NotFoundException(msg);
    }

    @Override
    public void removeFilm(Long id) {

    }

    @Override
    public Collection<Film> getTopFilms(Long count) {
        return List.of();
    }

    @Override
    public Collection<Film> getFilmsByTitle(String query) {
        return List.of();
    }

    @Override
    public Collection<Film> getFilmsByDirector(String query) {
        return List.of();
    }

    @Override
    public Collection<Film> getFilmsByTitleAndDirector(String query) {
        return List.of();
    }

    @Override
    public Collection<Film> getCommonFilms(Long userId, Long filmId) {
        return List.of();
    }

    @Override
    public Collection<Film> getRecommendations(Long id) {
        return List.of();
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}