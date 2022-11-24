package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class FriendsDao {

    private final JdbcTemplate jdbcTemplate;
    private final UsersDao userStorage;

    public void addFriend(Integer id, Integer friendId) {
        userStorage.checkUserExist(id);
        userStorage.checkUserExist(friendId);

        String sql = "INSERT INTO friends(id_user, id_friend) VALUES (?, ?)";
        jdbcTemplate.update(sql, id, friendId);
        log.info("Added friendship for user with id: {} from other user with id: {}", id, friendId);
    }

    public void removeFriend(Integer id, Integer idUser) {
        userStorage.checkUserExist(id);
        userStorage.checkUserExist(idUser);

        String sql = "DELETE FROM friends WHERE id_user = ? AND id_friend = ?";
        jdbcTemplate.update(sql, id, idUser);
        log.info("Friendship of user with id: {} and other user with id: {} removed", id, idUser);
    }

    public Collection<User> getFriends(Integer id) {
        userStorage.checkUserExist(id);

        String sql = "SELECT * FROM users u, friends f WHERE f.id_user = ? AND u.id = f.id_friend";

        return jdbcTemplate.query(sql, (rs, rowNum) -> userStorage.makeUser(rs), id);
    }

    public Collection<User> getCommonFriends(Integer id, Integer otherId) {
        userStorage.checkUserExist(id);
        userStorage.checkUserExist(otherId);

        String sql = "SELECT * FROM users u, friends f, friends o WHERE u.id = f.id_friend AND u.id = o.id_friend " +
                "AND f.id_user = ? AND o.id_user = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> userStorage.makeUser(rs),
                id, otherId);
    }
}
