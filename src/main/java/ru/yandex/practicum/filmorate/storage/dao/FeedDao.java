package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Component
public class FeedDao implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UsersDao usersDao;

    public FeedDao(JdbcTemplate jdbcTemplate, UsersDao usersDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.usersDao = usersDao;
    }

    @Override
    public Collection<Feed> getFeedByUserId(int id) {
        usersDao.checkUserExist(id);

        String sql = "select * from feed where id_user = ? order by timestamp asc";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFeed(rs), id);
    }

    @Override
    public void addFeed(int idEntity, int idUser, long timestamp, EventType eventType, Operation operation) {

        String sql = "insert into feed(id_entity, id_user, timestamp, event_type, operation) " +
                "values (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, idEntity, idUser, timestamp,
                eventType.toString(), operation.toString());
    }

    private Feed makeFeed(ResultSet rs) throws SQLException {
        return Feed.builder()
                .eventId(rs.getLong("id_event"))
                .entityId(rs.getInt("id_entity"))
                .userId(rs.getInt("id_user"))
                .timestamp(rs.getLong("timestamp"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(Operation.valueOf(rs.getString("operation")))
                .build();
    }
}
