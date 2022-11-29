package ru.yandex.practicum.filmorate.storage.dao;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;


@Slf4j
@Component
@RequiredArgsConstructor
public class FilmsDao implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films(name, description, releasedate, duration, mpa)" +
                " VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        int id = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(id);

        film.setMpa(mpaService.getById(film.getMpa().getId()));

        if (!isEmpty(film.getGenres())) {
            List<List<Genre>> batchLists = Lists.partition(film.getGenres(), film.getGenres().size());

            String sql2 = "INSERT INTO film_genres(id_film, id_genre) VALUES (?, ?)";

            for (List<Genre> batch : batchLists) {
                jdbcTemplate.batchUpdate(sql2, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Genre genre = batch.get(i);
                        ps.setInt(1, film.getId());
                        ps.setInt(2, genre.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return film.getGenres().size();
                    }
                });
            }
        }

        film.setGenres(getGenresByFilmId(film.getId()));

        if (!isEmpty(film.getDirectors())) {
            setFilmDirectors(film);
            film.setDirectors(getDirectorsByFilmId(film.getId()));
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        Film oldFilm = findById(film.getId()).orElseThrow();

        String sql = "UPDATE films SET name = ?, description = ?, releasedate = ?, duration = ?," +
                " mpa = ? WHERE id = ?";

        jdbcTemplate.update(sql, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), film.getMpa().getId(), film.getId());

        if (!Objects.equals(film.getGenres(), oldFilm.getGenres())) {
            String sql2 = "DELETE FROM film_genres WHERE id_film = ?";
            jdbcTemplate.update(sql2, film.getId());

            if (!isEmpty(film.getGenres())) {
                String sql3 = "INSERT INTO film_genres(id_genre, id_film) VALUES (?, ?)";

                for (int id : film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet())) {
                    jdbcTemplate.update(sql3, id, film.getId());
                }
            }
        }

        film.setMpa(mpaService.getById(film.getMpa().getId()));
        film.setGenres(getGenresByFilmId(film.getId()));

        deleteFilmDirectors(film);

        if (!isEmpty(film.getDirectors())) {
            setFilmDirectors(film);
            film.setDirectors(getDirectorsByFilmId(film.getId()));
        }

        return film;
    }

    @Override
    public Optional<Film> findById(Integer id) {
        String sql = "SELECT * FROM films WHERE id = ?";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeFilm(rs), id));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> findByDirectorIdSortedByYear(Integer directorId) {
        String sqlQuery = "SELECT * FROM films f "
                + "WHERE f.id IN (SELECT film_id FROM film_directors WHERE director_id = ?) "
                + "ORDER BY EXTRACT(YEAR FROM releasedate) ASC";

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), directorId)
                .stream()
                .map(film -> findById(film.getId()).orElseThrow())
                .collect(toList());
    }

    @Override
    public List<Film> findByDirectorIdSortedByLikes(Integer directorId) {
        String sqlQuery = "SELECT * FROM "
                + "(SELECT * FROM films f WHERE f.id IN (SELECT film_id FROM film_directors WHERE director_id = ?)) "
                + "LEFT JOIN (SELECT id_film, count(*) likes_count FROM likes_by_users GROUP BY id_film) l "
                + "ORDER BY likes_count ASC";

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), directorId)
                .stream()
                .map(film -> findById(film.getId()).orElseThrow())
                .collect(toList());
    }

    @Override
    public List<Film> searchFilms(String query, String groupBy) {
        String sql;
        switch (groupBy) {
            case "title":
                sql = "select * from films as f where locate(?, lower(name)) > 0";
                return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), query.toLowerCase());
            case "director":
                sql = "select * from films as f, film_directors as fd, directors as d " +
                        "where f.id = fd.film_id and fd.director_id = d.id and locate(?, lower(d.name)) > 0";
                return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), query.toLowerCase());
            case "director,title":
            case "title,director":
                sql = "select * from films as f, film_directors as fd, directors as d " +
                        "where (locate(?, lower(f.name)) > 0 or (f.id = fd.film_id and fd.director_id = d.id and locate(?, lower(d.name)) > 0))";
                List<Film> ans = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), query.toLowerCase(), query.toLowerCase());
                HashSet<Film> uniqueList = new HashSet<>(ans);
                ans = new ArrayList<>();
                ans.addAll(uniqueList);
                Collections.reverse(ans);
                return ans;
            default:
                throw new BadRequestException("Incorrect parameters value");
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM films where id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        String sql = "SELECT * FROM films f WHERE id IN " +
                "(SELECT l1.id_film FROM likes_by_users l1, likes_by_users l2 " +
                "WHERE l1.id_film = l2.id_film AND l1.id_user = ? AND l2.id_user = ? " +
                "GROUP BY l1.id_film ORDER BY COUNT(l1.id_user) DESC)";

        return jdbcTemplate.query(sql, (rs, rowMap) -> makeFilm(rs), userId, friendId);
    }

    private List<Genre> getGenresByFilmId(int filmId) {
        String sql = "SELECT id_genre FROM film_genres WHERE id_film = ?";
        List<Integer> genresId = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("id_genre"), filmId);

        return genresId.stream().map(genreService::getById).collect(toList());
    }

    private List<Director> getDirectorsByFilmId(int filmId) {
        String sql = "SELECT director_id FROM film_directors WHERE film_id = ?";
        List<Integer> directorsId = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("director_id"), filmId);

        return directorsId.stream().map(directorService::getById).collect(toList());
    }

    private void setFilmDirectors(Film film) {
        String sqlQuery = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";

        film.getDirectors()
                .forEach(director ->
                        jdbcTemplate.update(connection -> {
                            PreparedStatement stmt = connection.prepareStatement(sqlQuery);
                            stmt.setLong(1, film.getId());
                            stmt.setInt(2, director.getId());
                            return stmt;
                        }));
    }

    private void deleteFilmDirectors(Film film) {
        String sqlQuery = "DELETE FROM film_directors WHERE film_id = ?";

        jdbcTemplate.update(sqlQuery, film.getId());
    }

    private Film makeFilm(ResultSet rs) throws SQLException {
        LocalDate releaseDate =
                rs.getDate("releaseDate") == null ?
                        null : rs.getDate("releaseDate").toLocalDate();
        return Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(releaseDate)
                .duration(rs.getInt("duration"))
                .mpa(mpaService.getById(rs.getInt("mpa")))
                .genres(getGenresByFilmId(rs.getInt("id")))
                .directors(getDirectorsByFilmId(rs.getInt("id")))
                .build();
    }
}
