package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class UserFriends {
    public Long user_id;
    public Long friend_id;
    public UserFriendshipStatus status;
}
