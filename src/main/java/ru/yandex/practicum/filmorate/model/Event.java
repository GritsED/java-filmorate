package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

@Data
@Builder
public class Event {
    Long id;
    Long entityId;
    EventType eventType;
    Operation operation;
    Long userId;
    long timestamp;
}
