package ru.yandex.practicum.filmorate.storage.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MPAService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MPAService mpaService;
    private final GenreService genreService;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, MPAService mpaService, GenreService genreService) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaService = mpaService;
        this.genreService = genreService;

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

        if (!CollectionUtils.isEmpty(film.getGenres())) {
            String sql2 = "insert into film_genres(id_film, id_genre) values (?, ?)";

            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sql2, film.getId(), genre.getId());
            }
        }

        film.setGenres(getGenresByFilmId(film.getId()));

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

            if (!CollectionUtils.isEmpty(film.getGenres())) {
                String sql3 = "insert into film_genres(id_genre, id_film) values (?, ?)";

                for (int id : film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet())) {
                    jdbcTemplate.update(sql3, id, film.getId());
                }
            }
        }

        film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
        film.setGenres(getGenresByFilmId(film.getId()));

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

    private List<Genre> getGenresByFilmId(int filmId) {
        String sql = "select id_genre from film_genres where id_film = ?";
        List<Integer> genresId = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("id_genre"), filmId);

        return genresId.stream().map(genreService::getGenreById).collect(Collectors.toList());
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
                .build();
    }
}
