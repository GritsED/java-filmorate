package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


class UserControllerTest {

    UserController uc;
    User user;
    User user2;


    @BeforeEach
    void init() {
        uc = new UserController();
        user = User.builder()
                .email("qwer@qwer.ru")
                .login("Loss")
                .name("Los")
                .birthday(LocalDate.of(1999, 2, 11))
                .build();

        user2 = User.builder()
                .email("12345@qwer.ru")
                .login("QWER")
                .birthday(LocalDate.of(1999, 11, 11))
                .build();
    }

    @Test
    void create_shouldCreateUsers() {
        uc.create(user);
        uc.create(user2);

        assertEquals(2, uc.findAll().size());
    }

    @Test
    void create_shouldNotCreateUserWithWrongEmail() {
        User userWithoutEmail = User.builder()
                .email("qwerqwer.ru")
                .login("Loss")
                .name("Los")
                .birthday(LocalDate.of(1999, 2, 11))
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> uc.create(userWithoutEmail));
        assertEquals("Uncorrected email. Please use a valid format like example@domain.com", exception.getMessage());
    }

    @Test
    void create_shouldNotCreateUserWithSameEmail() {
        User userWithSameEmail = User.builder()
                .email("qwer@qwer.ru")
                .login("5432")
                .name("Los")
                .birthday(LocalDate.of(1999, 2, 11))
                .build();
        uc.create(user);

        ValidationException exception = assertThrows(ValidationException.class, () -> uc.create(userWithSameEmail));
        assertEquals("This email " + user.getEmail() + " is already used", exception.getMessage());
    }

    @Test
    void create_shouldNotCreateUserWithoutLogin() {
        User userWithoutLogin = User.builder()
                .email("qwer@qwer.ru")
                .name("Los")
                .birthday(LocalDate.of(1999, 2, 11))
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> uc.create(userWithoutLogin));
        assertEquals("Login cannot be empty or contain spaces", exception.getMessage());
    }

    @Test
    void create_shouldNotCreateUserWithSameLogin() {
        User userWithSameEmail = User.builder()
                .email("qwer123@qwer.ru")
                .login("Loss")
                .name("Los")
                .birthday(LocalDate.of(1999, 2, 11))
                .build();
        uc.create(user);

        ValidationException exception = assertThrows(ValidationException.class, () -> uc.create(userWithSameEmail));
        assertEquals("Login " + user.getLogin() + " is already used", exception.getMessage());
    }

    @Test
    void create_shouldNotCreateUserWithWrongDateOfBirth() {
        User userWithSameEmail = User.builder()
                .email("qwer123@qwer.ru")
                .login("Loss")
                .name("Los")
                .birthday(LocalDate.of(2025, 2, 11))
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> uc.create(userWithSameEmail));
        assertEquals("Date of birth cannot be in the future.", exception.getMessage());
    }

    @Test
    void update_shouldUpdateUser() {
        uc.create(user);
        User userUpd = User.builder()
                .id(1L)
                .email("qwer@qwer.ru")
                .login("LossUPD")
                .name("Los")
                .birthday(LocalDate.of(1999, 2, 11))
                .build();

        uc.updateUser(userUpd);
        String login = uc.findAll().stream().toList().getFirst().getLogin();
        assertEquals("LossUPD", login);
    }

    @Test
    void update_shouldNotUpdateUserWithoutId() {
        uc.create(user);
        User userUpd = User.builder()
                .email("qwer@qwer.ru")
                .login("LossUPD")
                .name("Los")
                .birthday(LocalDate.of(1999, 2, 11))
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> uc.updateUser(userUpd));
        assertEquals("ID must be specified.", exception.getMessage());
    }

    @Test
    void update_shouldNotUpdateUserWithWrongId() {
        uc.create(user);
        User userUpd = User.builder()
                .id(3L)
                .email("qwer@qwer.ru")
                .login("LossUPD")
                .name("Los")
                .birthday(LocalDate.of(1999, 2, 11))
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> uc.updateUser(userUpd));
        assertEquals("User with id  = " + userUpd.getId() + " not found.", exception.getMessage());
    }

    @Test
    void update_shouldNotUpdateUserWithoutEmail() {
        uc.create(user);
        User userUpd = User.builder()
                .id(1L)
                .login("LossUPD")
                .name("Los")
                .birthday(LocalDate.of(1999, 2, 11))
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> uc.updateUser(userUpd));
        assertEquals("Uncorrected email. Please use a valid format like example@domain.com", exception.getMessage());
    }

    @Test
    void update_shouldNotUpdateUserWithoutLogin() {
        uc.create(user);
        User userUpd = User.builder()
                .id(1L)
                .email("qwer@qwer.ru")
                .name("Los")
                .birthday(LocalDate.of(1999, 2, 11))
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> uc.updateUser(userUpd));
        assertEquals("Login cannot be empty or contain spaces", exception.getMessage());
    }

    @Test
    void update_shouldNotUpdateUserWithWrongDateOfBirth() {
        uc.create(user);
        User userUpd = User.builder()
                .id(1L)
                .email("qwer@qwer.ru")
                .login("LossUPD")
                .name("Los")
                .birthday(LocalDate.of(2025, 2, 11))
                .build();

        ValidationException exception = assertThrows(ValidationException.class, () -> uc.updateUser(userUpd));
        assertEquals("Date of birth cannot be in the future.", exception.getMessage());
    }

}