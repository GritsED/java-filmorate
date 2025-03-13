package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

@Data
@Builder
public class Event {
    @JsonProperty("eventId")
    Long id;
    Long entityId;
    EventType eventType;
    Operation operation;
    Long userId;
    long timestamp;
}
