package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;

@Component
public class FeedDao {

    private final JdbcTemplate jdbcTemplate;
    private final UsersDao usersDao;

    public FeedDao(JdbcTemplate jdbcTemplate, UsersDao usersDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.usersDao = usersDao;
    }

    public Collection<Feed> getFeedByUserId(int id) {
        usersDao.checkUserExist(id);

        String sql = "select * from feed where id_user = ? order by timestamp desc";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFeed(rs), id);
    }

    public void addFeed(int idEntity, int idUser, EventType eventType, Operation operation) {
        Instant timestamp = Instant.now();

        String sql = "insert into feed(id_entity, id_user, timestamp, event_type, operation) " +
                "values (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, idEntity, idUser, Timestamp.from(timestamp),
                eventType.toString(), operation.toString());
    }

    private Feed makeFeed(ResultSet rs) throws SQLException {
        return Feed.builder()
                .idEvent(rs.getLong("id_event"))
                .idEntity(rs.getInt("id_entity"))
                .idUser(rs.getInt("id_user"))
                .timestamp(rs.getTimestamp("timestamp").toInstant())
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(Operation.valueOf(rs.getString("operation")))
                .build();
    }
}
