package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("friendDbStorage") FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    public void addFriend(Long userId, Long user2Id) {
        friendshipStorage.addFriend(userId, user2Id);
    }

    public void removeFriend(Long userId, Long friendId) {
        friendshipStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getUserFriends(Long id) {
        return friendshipStorage.getUserFriends(id);
    }

    public Collection<User> getCommonFriends(Long userId, Long user2Id) {
        return friendshipStorage.getCommonFriends(userId, user2Id);
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findUser(Long id) {
        return userStorage.findUser(id);
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User newUser) {
        return userStorage.updateUser(newUser);
    }

    public void removeUser(Long id) {
        userStorage.removeUser(id);
    }
}