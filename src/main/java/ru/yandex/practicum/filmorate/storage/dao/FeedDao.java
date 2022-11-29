package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.FeedMapper;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FeedDao implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FeedMapper feedRowMapper;

    @Override
    public List<Feed> findByUserId(int id) {

        String sql = "select * from feed where id_user = ? order by timestamp asc";

        return jdbcTemplate.query(sql, feedRowMapper, id);
    }

    @Override
    public void addFeed(int idEntity, int idUser, long timestamp, EventType eventType, Operation operation) {

        String sql = "insert into feed(id_entity, id_user, timestamp, event_type, operation) " +
                "values (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, idEntity, idUser, timestamp,
                eventType.toString(), operation.toString());
    }
}
