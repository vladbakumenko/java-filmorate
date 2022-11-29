package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikesDao {

    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;

    public void addLike(Integer idFilm, Integer idUser) {
        filmStorage.findById(idFilm);
        removeLike(idFilm, idUser);

        String sql = "insert into likes_by_users(id_film, id_user) values (?, ?)";
        jdbcTemplate.update(sql, idFilm, idUser);
        log.info("Like added for film with id: {} from user with id: {}", idFilm, idUser);
    }

    public void removeLike(Integer idFilm, Integer idUser) {
        filmStorage.findById(idFilm);

        String sql = "delete from likes_by_users where id_film = ? and id_user = ?";
        jdbcTemplate.update(sql, idFilm, idUser);
        log.info("Like removed for film with id: {} from user with id: {}", idFilm, idUser);
    }

    public List<Film> getPopular(Integer count, Optional<Integer> genreId, Optional<Integer> year) {
        final String sqlQuery = "select * from films f "
                + "left join (select id_film, count(*) likes_count from likes_by_users group by id_film) l on f.id = l.id_film "
                + "order by l.likes_count desc limit ?";

        var stream = jdbcTemplate
                .query(sqlQuery, (rs, rowNum) -> filmStorage.findById(rs.getInt("id")), count)
                .stream()
                .map(film -> filmStorage.findById(film.orElseThrow().getId()));

        if (year.isPresent()) {
            stream = stream.filter(film -> film.orElseThrow().getReleaseDate().getYear() == year.get());
        }
        if (genreId.isPresent()) {
            stream = stream.filter(film -> film.orElseThrow().getGenres()
                    .stream().anyMatch(genre -> genre.getId() == genreId.get()));
        }

        return stream.map(Optional::orElseThrow).collect(toList());
    }

    public List<Film> getRecommendedFilm(Integer idUser, Integer recommendedIdUser) {
        log.info("Request to get recommended film from user with id: {}", idUser);
        String sql2 = "SELECT * FROM films AS f " +
                "   JOIN mpa AS m ON f.mpa = m.id " +
                "   WHERE f.id IN (SELECT id_film FROM likes_by_users WHERE id_user = ? AND " +
                "   id_film NOT IN (SELECT id_film FROM likes_by_users WHERE id_user = ?))";
        try {
            return new ArrayList<>(jdbcTemplate.query(sql2,
                    (rs, rowNum) -> filmStorage.findById(rs.getInt("id")).orElseThrow(),
                    recommendedIdUser, idUser));
        } catch (EmptyResultDataAccessException e) {
            log.debug("No record found in database for " + recommendedIdUser, e);
            return null;
        }
    }

    public Integer getUsersWithMaximumIntersectionLikes(Integer idUser) {
        log.info("Request to get user with maximum intersection likes from user with id: {}", idUser);
        String sql = "SELECT l2.id_user AS recommended" +
                "   FROM likes_by_users l1 JOIN likes_by_users l2 " +
                "   ON l1.id_film = l2.id_film " +
                "   WHERE l1.id_user = ? AND l1.id_user <> l2.id_user" +
                "   GROUP BY l1.id_user, l2.id_user" +
                "   ORDER BY COUNT(*) DESC LIMIT 1";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, idUser);
        } catch (EmptyResultDataAccessException e) {
            log.debug("No record found in database for " + idUser, e);
            return idUser;
        }
    }
}
