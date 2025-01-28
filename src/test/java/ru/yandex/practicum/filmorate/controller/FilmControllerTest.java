package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {
    FilmController fc;
    Film film;
    Film film2;
    FilmStorage fs;

    @BeforeEach
    void init() {
        fs = new InMemoryFilmStorage();
        fc = new FilmController(fs);
        film = Film.builder()
                .name("Harry Potter")
                .description("Description")
                .duration(100)
                .releaseDate(LocalDate.of(1999, 2, 10))
                .build();

        film2 = Film.builder()
                .name("Harry Potter2")
                .description("Description2")
                .duration(120)
                .releaseDate(LocalDate.of(1999, 9, 10))
                .build();
    }

    @Test
    void createFilm_shouldCreateFilm() {

        fc.create(film);
        fc.create(film2);

        assertEquals(2, fc.findAll().size());
    }

    @Test
    void updateFilm_shouldUpdateFilm() {
        fc.create(film);
        Film film3 = Film.builder()
                .id(1L)
                .name("Harry Potter UPD")
                .description("DescriptionUPD")
                .duration(100)
                .releaseDate(LocalDate.of(1999, 2, 10))
                .build();

        fc.updateFilm(film3);
        String description = fc.findAll().stream().toList().getFirst().getDescription();

        assertEquals("DescriptionUPD", description);

    }

    @Test
    void updateFilm_shouldNotUpdateFilmWithoutId() {
        fc.create(film);
        Film film3 = Film.builder()
                .name("Harry Potter UPD")
                .description("DescriptionUPD")
                .duration(100)
                .releaseDate(LocalDate.of(1999, 2, 10))
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> fc.updateFilm(film3));
        assertEquals("ID must be specified.", exception.getMessage());
    }

    @Test
    void updateFilm_shouldNotUpdateFilmWithWrongId() {
        fc.create(film);
        Film film3 = Film.builder()
                .id(3L)
                .name("Harry Potter UPD")
                .description("DescriptionUPD")
                .duration(100)
                .releaseDate(LocalDate.of(1999, 2, 10))
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> fc.updateFilm(film3));
        assertEquals("Film with id = " + film3.getId() + " not found.", exception.getMessage());
    }

}