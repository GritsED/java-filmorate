package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.film.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    public DirectorService(@Qualifier("directorDbStorage") DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Collection<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director findDirector(Long id) {
        return directorStorage.findDirector(id)
                .orElseThrow(() -> new NotFoundException("Film with id = " + id + " not found"));
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.updateDirector(director);
    }

    public void removeDirector(Long id) {
        directorStorage.removeDirector(id);
    }
}
