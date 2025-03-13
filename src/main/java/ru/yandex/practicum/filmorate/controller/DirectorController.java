package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    @Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public Collection<Director> findAll() {
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public Director findDirector(@PathVariable Long id) {
        return directorService.findDirector(id);
    }

    @PostMapping
    public Director create(@Valid @RequestBody Director director) {
        return directorService.create(director);
    }

    @PutMapping
    public Director updateDirector(@RequestBody Director director) {
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        if (id == null) throw new ValidationException("IDs must not be null");
        directorService.removeDirector(id);
    }
}
