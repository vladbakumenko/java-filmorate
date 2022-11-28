package ru.yandex.practicum.filmorate.storage.dao;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectorDao implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Director> directorRowMapper;

    @Override
    public List<Director> findAll() {
        String sql = "SELECT * FROM directors";

        return jdbcTemplate.query(sql, directorRowMapper);
    }

    @Override
    public Optional<Director> findById(Integer id) {
        String sql = "SELECT * FROM directors WHERE id = ?";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, directorRowMapper, id));
        } catch (DataAccessException e) {
            log.error("No record found in database for " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Director> add(Director director) {
        String sql = "INSERT INTO directors(name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        int id = requireNonNull(keyHolder.getKey()).intValue();
        director.setId(id);

        return Optional.of(director);
    }

    @Override
    public Optional<Director> update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";

        jdbcTemplate.update(sql, director.getName(), director.getId());

        return Optional.of(director);
    }

    @Override
    public void delete(Integer id) {
        String sqlQuery = "DELETE FROM film_directors WHERE director_id = ?";
        jdbcTemplate.update(sqlQuery, id);

        String sql = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
