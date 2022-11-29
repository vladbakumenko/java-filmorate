package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaDao {

    private final JdbcTemplate jdbcTemplate;

    public Optional<Mpa> findMpaById(int idMPA) {
        String sql = "select id, name, description from mpa where id = ?";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Mpa(rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description")), idMPA));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Mpa> findAll() {
        String sql = "select * from mpa";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Mpa(rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description")));
    }
}
