package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class MpaDao {

    private final JdbcTemplate jdbcTemplate;

    public Mpa getMpaById(int idMPA) {
        String sql = "select id, name, description from mpa where id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Mpa(rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description")), idMPA);
        } catch (DataAccessException e) {
            throw new ObjectNotFoundException(String.format("Rating MPA with id: %d not found in DB", idMPA));
        }
    }

    public Collection<Mpa> findAll() {
        String sql = "select * from mpa";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Mpa(rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description")));
    }
}
