package ru.yandex.practicum.filmorate.mapper;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Review.builder().reviewId(rs.getInt("id")).
                    content(rs.getString("content")).
                    isPositive(rs.getBoolean("is_positive")).
                    userId(rs.getInt("id_user")).
                    filmId(rs.getInt("id_film")).
                    useful(rs.getInt("useful")).
                    build();
        }
    }
