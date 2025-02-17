package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Friendship {
    Long user_id;
    Long friend_id;
    boolean status;
}
