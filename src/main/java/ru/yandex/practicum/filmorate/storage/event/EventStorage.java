package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.util.Collection;

public interface EventStorage {
    boolean add(Long entityId, Long userId, EventType eventType, Operation operation);

    boolean remove(long id);

    Collection<Event> findAll();

    Collection<Event> findByUserId(Long id);

    Event find(long id);
}
