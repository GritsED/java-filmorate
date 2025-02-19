package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();

    Optional<User> findUser(Long id);

    User create(User user);

    User updateUser(User newUser);

    void removeUser(Long id);
}
