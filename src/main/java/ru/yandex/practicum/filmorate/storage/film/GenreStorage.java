package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;

public interface GenreStorage {
    Genre findGenre(Integer id);

    Collection<Genre> findAll();

    void getFilmsGenres(List<Film> films);
}