package ru.yandex.practicum.filmorate.storage.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import static org.springframework.util.CollectionUtils.isEmpty;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MPAService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;


@Slf4j
@Component
public class FilmsDao implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MPAService mpaService;
    private final GenreService genreService;
    private final DirectorService directorService;

    public FilmsDao(JdbcTemplate jdbcTemplate, MPAService mpaService, GenreService genreService,
                    DirectorService directorService) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.directorService = directorService;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "select * from films";

        return jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs));
    }

    @Override
    public Film create(Film film) {
        String sql = "insert into films(name, description, releaseDate, duration, mpa)" +
                " values (?, ?, ?, ?, ?)";

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

        film.setMpa(mpaService.getMpaById(film.getMpa().getId()));

        if (!isEmpty(film.getGenres())) {
            List<List<Genre>> batchLists = Lists.partition(film.getGenres(), film.getGenres().size());

            String sql2 = "insert into film_genres(id_film, id_genre) values (?, ?)";

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
        Film oldFilm = getById(film.getId());

        String sql = "update films set name = ?, description = ?, releaseDate = ?, duration = ?," +
                " mpa = ? where id = ?";

        jdbcTemplate.update(sql, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()),
                film.getDuration(), film.getMpa().getId(), film.getId());

        if (!Objects.equals(film.getGenres(), oldFilm.getGenres())) {
            String sql2 = "delete from film_genres where id_film = ?";
            jdbcTemplate.update(sql2, film.getId());

            if (!isEmpty(film.getGenres())) {
                String sql3 = "insert into film_genres(id_genre, id_film) values (?, ?)";

                for (int id : film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet())) {
                    jdbcTemplate.update(sql3, id, film.getId());
                }
            }
        }

        film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
        film.setGenres(getGenresByFilmId(film.getId()));

        deleteFilmDirectors(film);

        if (!isEmpty(film.getDirectors())) {
            setFilmDirectors(film);
            film.setDirectors(getDirectorsByFilmId(film.getId()));
        }

        return film;
    }

    @Override
    public Film getById(Integer id) {
        String sql = "select * from films where id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeFilm(rs), id);
        } catch (DataAccessException e) {
            throw new FilmNotFoundException(String.format("Film with id: %d not found", id));
        }
    }

    @Override
    public List<Film> getSorted(Integer directorId, String param) {
        String sqlQuery = "";

        if (param.equals("year")) {
            sqlQuery = "select * from films f "
                    + "where f.id in (select film_id from film_directors where director_id = ?) "
                    + "order by EXTRACT(YEAR FROM releasedate) asc";
        } else if (param.equals("likes")) {
            sqlQuery = "select * from "
                    + "(select * from films f where f.id in (select film_id from film_directors where director_id = ?)) "
                    + "left join (select id_film, count(*) likes_count FROM likes_by_users GROUP BY id_film) L "
                    + "order by likes_count asc";
        } else throw new ValidationException("Incorrect parameters value");

        directorService.getById(directorId);

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), directorId)
                .stream()
                .map(film -> getById(film.getId()))
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
                        "where (f.id = fd.film_id and fd.director_id = d.id and locate(?, lower(d.name)) > 0)";
                List<Film> ans = jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), query.toLowerCase());
                sql = "select * from films as f where locate(?, lower(name)) > 0";
                ans.addAll(jdbcTemplate.query(sql, (rs, rowNum) -> makeFilm(rs), query.toLowerCase()));
                return ans;
            default:
                throw new ValidationException("Incorrect parameters value");
        }
    }

    private List<Genre> getGenresByFilmId(int filmId) {
        String sql = "select id_genre from film_genres where id_film = ?";
        List<Integer> genresId = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("id_genre"), filmId);

        return genresId.stream().map(genreService::getGenreById).collect(toList());
    }

    private List<Director> getDirectorsByFilmId(int filmId) {
        String sql = "select director_id from film_directors where film_id = ?";
        List<Integer> directorsId = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("director_id"), filmId);

        return directorsId.stream().map(directorService::getById).collect(toList());
    }

    private void setFilmDirectors(Film film) {
        String sqlQuery = "insert into film_directors (film_id, director_id) values (?, ?)";

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
        String sqlQuery = "delete from film_directors where film_id = ?";

        jdbcTemplate.update(sqlQuery, film.getId());
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM films where id = ?";
        try {
            jdbcTemplate.update(sql, id);
        } catch (DataAccessException e) {
            throw new FilmNotFoundException(String.format("Film with id: %d not found", id));
        }
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
                .mpa(mpaService.getMpaById(rs.getInt("mpa")))
                .genres(getGenresByFilmId(rs.getInt("id")))
                .directors(getDirectorsByFilmId(rs.getInt("id")))
                .build();
    }
}
