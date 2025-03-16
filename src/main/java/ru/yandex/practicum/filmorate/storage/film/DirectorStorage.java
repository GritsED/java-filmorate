package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {
    Director create(Director director);

    Director updateDirector(Director director);

    Optional<Director> findDirector(Long id);

    Collection<Director> findAll();

    void removeDirector(Long id);
}
