package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Friendship {
    Long userId;
    Long friendId;
    boolean status;
}
