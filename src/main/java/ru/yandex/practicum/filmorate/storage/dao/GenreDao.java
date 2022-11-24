package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Component
public class GenreDao {

    private final JdbcTemplate jdbcTemplate;

    public GenreDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Genre getGenreById(int idGenre) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")),
                    idGenre);
        } catch (DataAccessException e) {
            throw new ObjectNotFoundException(String.format("Genre with id: %d not found in DB", idGenre));
        }
    }

    public Collection<Genre> findAll() {
        String sql = "SELECT id, name FROM genres";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")));
    }

}
