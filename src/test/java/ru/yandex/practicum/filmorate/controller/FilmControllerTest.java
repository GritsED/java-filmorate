//package ru.yandex.practicum.filmorate.controller;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import ru.yandex.practicum.filmorate.exception.NotFoundException;
//import ru.yandex.practicum.filmorate.exception.ValidationException;
//import ru.yandex.practicum.filmorate.model.Film;
//import ru.yandex.practicum.filmorate.service.FilmService;
//import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
//import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
//import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
//import ru.yandex.practicum.filmorate.storage.user.UserStorage;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//class FilmControllerTest {
//    FilmController filmController;
//    FilmService filmService;
//    FilmStorage filmStorage = new InMemoryFilmStorage();
//    UserStorage userStorage = new InMemoryUserStorage();
//    Film film;
//    Film film2;
//
//    @BeforeEach
//    void init() {
//        filmService = new FilmService(filmStorage, userStorage);
//        filmController = new FilmController(filmService);
//        film = Film.builder()
//                .name("Harry Potter")
//                .description("Description")
//                .duration(100)
//                .releaseDate(LocalDate.of(1999, 2, 10))
//                .build();
//
//        film2 = Film.builder()
//                .name("Harry Potter2")
//                .description("Description2")
//                .duration(120)
//                .releaseDate(LocalDate.of(1999, 9, 10))
//                .build();
//    }
//
//    @Test
//    void createFilm_shouldCreateFilm() {
//
//        filmController.create(film);
//        filmController.create(film2);
//
//        assertEquals(2, filmController.findAll().size());
//    }
//
//    @Test
//    void updateFilm_shouldUpdateFilm() {
//        filmController.create(film);
//        Film film3 = Film.builder()
//                .id(1L)
//                .name("Harry Potter UPD")
//                .description("DescriptionUPD")
//                .duration(100)
//                .releaseDate(LocalDate.of(1999, 2, 10))
//                .build();
//
//        filmController.updateFilm(film3);
//        String description = filmController.findAll().stream().toList().getFirst().getDescription();
//
//        assertEquals("DescriptionUPD", description);
//
//    }
//
//    @Test
//    void updateFilm_shouldNotUpdateFilmWithoutId() {
//        filmController.create(film);
//        Film film3 = Film.builder()
//                .name("Harry Potter UPD")
//                .description("DescriptionUPD")
//                .duration(100)
//                .releaseDate(LocalDate.of(1999, 2, 10))
//                .build();
//
//        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.updateFilm(film3));
//        assertEquals("ID must be specified.", exception.getMessage());
//    }
//
//    @Test
//    void updateFilm_shouldNotUpdateFilmWithWrongId() {
//        filmController.create(film);
//        Film film3 = Film.builder()
//                .id(3L)
//                .name("Harry Potter UPD")
//                .description("DescriptionUPD")
//                .duration(100)
//                .releaseDate(LocalDate.of(1999, 2, 10))
//                .build();
//
//        NotFoundException exception = assertThrows(NotFoundException.class, () -> filmController.updateFilm(film3));
//        assertEquals("Film with id = " + film3.getId() + " not found.", exception.getMessage());
//    }
//}