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
            return Optional.empty();
        }
    }

    @Override
    public Director add(Director director) {
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
        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        int directorId = director.getId();

        jdbcTemplate.update(sql, director.getName(), directorId);

        return director;
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
