package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDao {

    private final JdbcTemplate jdbcTemplate;

    public Optional<Genre> findGenreById(int idGenre) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")), idGenre));
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Genre> findAll() {
        String sql = "SELECT id, name FROM genres";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")));
    }
}
