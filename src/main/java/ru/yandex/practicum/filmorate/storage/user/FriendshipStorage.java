package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FriendshipStorage {
    void addFriend(Long userId, Long user2Id);

    void removeFriend(Long userId, Long friendId);

    Collection<User> getUserFriends(Long id);

    Collection<User> getCommonFriends(Long userId, Long user2Id);
}
