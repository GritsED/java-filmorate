package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (isValid(user)) {
            user.setId(getNextId());
            users.put(user.getId(), user);
            log.info("User has been added: {}", user);
        }
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            String msg = "ID must be specified";
            log.error(msg);
            throw new ValidationException(msg);
        }

        if (users.containsKey(newUser.getId())){
            User oldUser = users.get(newUser.getId());

            if (isValid(newUser)) {
                oldUser.setEmail(newUser.getEmail());
                oldUser.setLogin(newUser.getLogin());
                oldUser.setName(newUser.getName());
                oldUser.setBirthday(newUser.getBirthday());
                log.info("User with id = {} has benn updated", newUser.getId());
            }
            return oldUser;
        }
        String msg = "User with id  = " + newUser.getId() + " not found";
        log.error(msg);
        throw new ValidationException(msg);
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private boolean isValid(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            String msg = "Uncorrected email. Please use a valid format like example@domain.com";
            log.error(msg);
            throw new ValidationException(msg);
        }

        if (users.values().stream().anyMatch(u -> u.getEmail().equals(user.getEmail()))) {
            String msg = "This email is already used";
            log.error(msg);
            throw new ValidationException(msg);
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            String msg = "Login cannot be empty or contain spaces";
            log.error(msg);
            throw new ValidationException(msg);
        }

        if (users.values().stream().anyMatch(u -> u.getLogin().equals(user.getLogin()))) {
            String msg = "This login is already used";
            log.error(msg);
            throw new ValidationException(msg);
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            String msg = "Date of birth cannot be in the future.";
            log.error(msg);
            throw new ValidationException(msg);
        }
        return true;
    }
}
