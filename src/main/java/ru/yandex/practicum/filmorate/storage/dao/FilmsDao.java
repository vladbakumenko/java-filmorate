package ru.yandex.practicum.filmorate.storage.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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

        film.setMpa(mpaService.getMpaById(film.getMpa().getId()));

        if (!isEmpty(film.getGenres())) {
            List<List<Genre>> batchLists = Lists.partition(film.getGenres(), 1);

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
        Film oldFilm = getById(film.getId());

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
        String sql = "SELECT * FROM films WHERE id = ?";

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
            sqlQuery = "SELECT * FROM films f "
                    + "WHERE f.id IN (SELECT film_id FROM film_directors WHERE director_id = ?) "
                    + "ORDER BY EXTRACT(YEAR FROM releasedate) ASC";
        } else if (param.equals("likes")) {
            sqlQuery = "SELECT * FROM "
                    + "(SELECT * FROM films f WHERE f.id IN (SELECT film_id FROM film_directors WHERE director_id = ?)) "
                    + "LEFT JOIN (SELECT id_film, count(*) likes_count FROM likes_by_users GROUP BY id_film) l "
                    + "ORDER BY likes_count ASC";
        } else throw new ValidationException("Incorrect parameters value");

        directorService.getById(directorId);

        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> makeFilm(rs), directorId)
                .stream()
                .map(film -> getById(film.getId()))
                .collect(toList());
    }

    private List<Genre> getGenresByFilmId(int filmId) {
        String sql = "SELECT id_genre FROM film_genres WHERE id_film = ?";
        List<Integer> genresId = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("id_genre"), filmId);

        return genresId.stream().map(genreService::getGenreById).collect(toList());
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
        return Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("releaseDate").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpaService.getMpaById(rs.getInt("mpa")))
                .genres(getGenresByFilmId(rs.getInt("id")))
                .directors(getDirectorsByFilmId(rs.getInt("id")))
                .build();
    }

}
