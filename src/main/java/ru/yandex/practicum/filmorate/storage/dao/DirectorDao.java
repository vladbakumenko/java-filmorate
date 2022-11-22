package ru.yandex.practicum.filmorate.storage.dao;

import java.sql.PreparedStatement;
import java.util.Collection;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

@Component
@Slf4j
public class DirectorDao implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Director> directorMapper;

    public DirectorDao(JdbcTemplate jdbcTemplate, RowMapper<Director> directorMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.directorMapper = directorMapper;
    }

    @Override
    public Collection<Director> findAll() {
        String sql = "select * from directors";

        return jdbcTemplate.query(sql, directorMapper);
    }

    @Override
    public Director getById(Integer id) {
        String sql = "select * from directors where id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, directorMapper, id);
        } catch (DataAccessException e) {
            throw new ObjectNotFoundException(format("Director with id: %d not found in DB", id));
        }
    }

    @Override
    public Director create(Director director) {
        String sql = "insert into directors(name) values (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        int id = requireNonNull(keyHolder.getKey()).intValue();
        director.setId(id);

        return director;
    }

    @Override
    public Director update(Director director) {
        getById(director.getId());

        String sql = "update directors set name = ? where id = ?";

        jdbcTemplate.update(sql, director.getName(), director.getId());

        return director;
    }

    @Override
    public void delete(Integer id) {
        getById(id);

        String sqlQuery = "delete from film_directors where director_id = ?";
        jdbcTemplate.update(sqlQuery, id);

        String sql = "delete from directors where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
