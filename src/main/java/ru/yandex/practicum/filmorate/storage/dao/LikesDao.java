package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Component
public class LikesDao {

    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public LikesDao(JdbcTemplate jdbcTemplate, FilmStorage filmStorage, UserStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(Integer idFilm, Integer idUser) {
        filmStorage.getById(idFilm);
        userStorage.checkUserExist(idUser);

        String sql = "insert into likes_by_users(id_film, id_user) values (?, ?)";
        jdbcTemplate.update(sql, idFilm, idUser);
        log.info("Like added for film with id: {} from user with id: {}", idFilm, idUser);
    }

    public void removeLike(Integer idFilm, Integer idUser) {
        filmStorage.getById(idFilm);
        userStorage.checkUserExist(idUser);

        String sql = "delete from likes_by_users where id_film = ? and id_user = ?";
        jdbcTemplate.update(sql, idFilm, idUser);
        log.info("Like removed for film with id: {} from user with id: {}", idFilm, idUser);
    }

    public Collection<Film> getPopular(Integer count) {

        String sql = "select f.id from films as f join likes_by_users as l on f.id = l.id_film " +
                "group by f.id order by count(l.id_user) desc limit ?";

        Collection<Film> sortedFilmsByLikes = new LinkedList<>(jdbcTemplate.query(sql,
                (rs, rowNum) -> filmStorage.getById(rs.getInt("id")), count));

        Collection<Film> filmsWithoutLikes = new ArrayList<>();

        if (sortedFilmsByLikes.size() == 0 || sortedFilmsByLikes.size() < count) {
            int limit = count - sortedFilmsByLikes.size();
            String sql2 = "select id from films where id not in " +
                    "(select id_film from likes_by_users group by id_film) limit ?";
            filmsWithoutLikes.addAll(jdbcTemplate.query(sql2,
                    (rs, rowNum) -> filmStorage.getById(rs.getInt("id")), limit));
        }
        sortedFilmsByLikes.addAll(filmsWithoutLikes);

        return sortedFilmsByLikes;
    }

    private Integer getUsersWithMaximumIntersectionLikes (Integer idUser) {
        String sql = "SELECT l2.id_user AS recommended" +
                "   FROM likes_by_users l1 JOIN likes_by_users l2 " +
                "   ON l1.id_film = l2.id_film " +
                "   WHERE l1.id_user = ? AND l1.id_user <> l2.id_user" +
                "   GROUP BY l1.id_user, l2.id_user" +
                "   ORDER BY COUNT(*) DESC LIMIT 1";
        return jdbcTemplate.queryForObject(
                sql, Integer.class , idUser);
    }

    public Collection<Film> getRecommendedFilm(Integer idUser) {
        Integer recommendedUserId = getUsersWithMaximumIntersectionLikes (idUser);
        String sql2 = "SELECT * FROM films AS f " +
                "JOIN mpa AS m ON f.mpa = m.id " +
                "WHERE f.id IN (" +
                "    SELECT id_film" +
                "    FROM likes_by_users" +
                "    WHERE id_user = ?" +
                "AND id_film NOT IN (SELECT id_film FROM likes_by_users WHERE id_user = ?))";
        return new ArrayList<>(jdbcTemplate.query(sql2,
                (rs, rowNum) -> filmStorage.getById(rs.getInt("id")), recommendedUserId, idUser));
    }
}
