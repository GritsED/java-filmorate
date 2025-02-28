package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

public interface MpaStorage {
    Mpa findMpa(Integer id);

    Collection<Mpa> findAll();
}
