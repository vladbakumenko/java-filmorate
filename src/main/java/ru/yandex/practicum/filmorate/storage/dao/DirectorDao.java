package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.util.Collection;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Component
@RequiredArgsConstructor
public class DirectorDao implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Director> directorMapper;

    @Override
    public Collection<Director> findAll() {
        String sql = "SELECT * FROM directors";

        return jdbcTemplate.query(sql, directorMapper);
    }

    @Override
    public Director getById(Integer id) {
        String sql = "SELECT * FROM directors WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, directorMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException(format("Director with id: %d not found in DB", id));
        }
    }

    @Override
    public Director create(Director director) {
        String sql = "INSERT INTO directors(name) VALUES (?)";

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

        String sql = "UPDATE directors SET name = ? WHERE id = ?";

        jdbcTemplate.update(sql, director.getName(), director.getId());

        return director;
    }

    @Override
    public void delete(Integer id) {
        getById(id);

        String sqlQuery = "DELETE FROM film_directors WHERE director_id = ?";
        jdbcTemplate.update(sqlQuery, id);

        String sql = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
