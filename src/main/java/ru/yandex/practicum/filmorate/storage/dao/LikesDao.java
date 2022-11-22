package ru.yandex.practicum.filmorate.storage.dao;

import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

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

    public Collection<Film> getPopular(Integer count, Optional<Integer> genreId, Optional<Integer> year) {
        final String sqlQuery = "select * from films f "
                + "left join (select id_film, count(*) likes_count from likes_by_users group by id_film) l on f.id = l.id_film "
                + "left join mpa on f.id = mpa.id order by l.likes_count desc limit ?";

        var stream = jdbcTemplate
                .query(sqlQuery, (rs, rowNum) -> filmStorage.getById(rs.getInt("id")), count)
                .stream()
                .map(film -> filmStorage.getById(film.getId()));

        if (year.isPresent()) {
            stream = stream.filter(film -> film.getReleaseDate().getYear() == year.get());
        }
        if (genreId.isPresent()) {
            stream = stream.filter(film -> film.getGenres()
                    .stream().anyMatch(genre -> genre.getId() == genreId.get()));
        }

        return stream.collect(toList());
    }
}
