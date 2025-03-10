package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {
    private static final String GET_ALL_EVENTS = """
            SELECT *
            FROM events
            ORDER BY created_at ASC
            """;
    private static final String GET_EVENT_BY_ID = """
            SELECT *
            FROM events
            WHERE id = ?
            """;
    private static final String GET_EVENT_BY_USER_ID = """
            SELECT *
            FROM events
            WHERE user_id = ?
            ORDER BY created_at ASC
            """;
    private static final String INSERT_EVENT = """
            INSERT INTO events(user_id, event_type, operation, entity_id, created_at)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String DELETE_EVENT = """
            DELETE FROM events
            WHERE id = ?
            """;
    private final JdbcTemplate jdbc;
    private final EventRowMapper eventRowMapper;

    @Override
    public boolean add(Long entityId, Long userId, EventType eventType, Operation operation) {
        long timestampNow = System.currentTimeMillis();

        List<Object> params = Arrays.asList(userId,
                eventType.name(),
                operation.name(),
                entityId,
                timestampNow
        );
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_EVENT, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.size(); idx++) {
                ps.setObject(idx + 1, params.get(idx));
            }
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);

        return id != null;
    }

    @Override
    public boolean remove(long id) {
        int rowsAffected = jdbc.update(DELETE_EVENT, id);
        return rowsAffected > 0;
    }

    @Override
    public Collection<Event> findAll() {
        return jdbc.query(GET_ALL_EVENTS, eventRowMapper);
    }

    @Override
    public Collection<Event> findByUserId(Long id) {
        try {
            return jdbc.query(GET_EVENT_BY_USER_ID, eventRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("User with provided Id is not found");
        }
    }

    @Override
    public Event find(long id) {
        try {
            return jdbc.queryForObject(GET_EVENT_BY_ID, eventRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("User with provided Id is not found");
        }
    }
}
