package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventStorage eventStorage;

    public boolean add(Long entityId, Long userId, EventType eventType, Operation operation) {
        return eventStorage.add(entityId, userId, eventType, operation);
    }

    public boolean remove(long id) {
        return eventStorage.remove(id);
    }

    public Collection<Event> findAll() {
        return eventStorage.findAll();
    }

    public Collection<Event> findByUserId(Long id) {
        return eventStorage.findByUserId(id);
    }

    public Event find(long id) {
        return eventStorage.find(id);
    }
}
