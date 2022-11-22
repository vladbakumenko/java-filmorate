package ru.yandex.practicum.filmorate.storage.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewDao implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    public ReviewDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review getById(Integer id) {
        String sql = "SELECT * FROM reviews WHERE id =?";
        try {
        return jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> makeReview(rs), id);
        } catch (DataAccessException da) {
            throw new ObjectNotFoundException("Review with id: " + id + " not found");
        }
    }

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews(content,is_positive,id_user,id_film,useful) VALUES(?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement stmt = con.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            stmt.setInt(5, review.getUseful());
            return stmt;
        }, keyHolder);
        Integer id = keyHolder.getKey().intValue();
        review.setReviewId(id);
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?," +
                "is_positive=? where id=?";
        jdbcTemplate.update(sql, review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        return review;
    }

    @Override
    public void removeReviewById(Integer id) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Review> findAll(Integer filmId, Integer count) {
        if (filmId != null) {
            String sql = "SELECT * FROM reviews WHERE id_film=? ORDER BY useful DESC ";
            List<Review> reviews = jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs), filmId).
                    stream().
                    limit(count).
                    collect(Collectors.toList());
            return reviews;
        } else {
            String sql = "SELECT * FROM reviews ORDER BY useful DESC ";
            List<Review> reviews = jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs));
            return reviews.stream().limit(count).collect(Collectors.toList());
        }
    }

    public void addLikeReview(Integer reviewId, Integer userId) {
        String sql = "INSERT INTO feed(id_entity,id_user,timestamp,event_type,operation) VALUES(?,?,?,?,?)";
        jdbcTemplate.update(sql, reviewId, userId, LocalDate.now(), "LIKE", "ADD");
        String sql1 = "UPDATE reviews SET useful=useful+1 WHERE id=?";
        jdbcTemplate.update(sql1, reviewId);

    }

    public void addDislikeReview(Integer reviewId, Integer userId) {
        String sql = "INSERT INTO feed(id_entity,id_user,timestamp,event_type,operation) VALUES(?,?,?,?,?)";
        jdbcTemplate.update(sql, reviewId, userId, LocalDate.now(), "DISLIKE", "ADD");
        String sql1 = "UPDATE reviews SET useful=useful-1 WHERE id=?";
        jdbcTemplate.update(sql1, reviewId);
    }

    public void removeLikeReview(Integer reviewId, Integer userId) {
        String sql = "INSERT INTO feed(id_entity,id_user,timestamp,event_type,operation) VALUES(?,?,?,?,?)";
        jdbcTemplate.update(sql, reviewId, userId, LocalDate.now(), "LIKE", "REMOVE");
        String sql1 = "UPDATE reviews SET useful-=? WHERE id=?";
        jdbcTemplate.update(sql1, reviewId);
    }

    public void removeDislikeReview(Integer reviewId, Integer userId) {
        String sql = "INSERT INTO feed(id_entity,id_user,timestamp,event_type,operation) VALUES(?,?,?,?,?)";
        jdbcTemplate.update(sql, reviewId, userId, LocalDate.now(), "DISLIKE", "REMOVE");
        String sql1 = "UPDATE reviews SET useful=(useful+1) WHERE id=?";
        jdbcTemplate.update(sql1, reviewId);
    }

    private Review makeReview(ResultSet rs) throws SQLException {
        return Review.builder().reviewId(rs.getInt("id")).
                content(rs.getString("content")).
                isPositive(rs.getBoolean("is_positive")).
                userId(rs.getInt("id_user")).
                filmId(rs.getInt("id_film")).
                useful(rs.getInt("useful")).
                build();

    }

}
