package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.dao.ReviewDao;

import javax.validation.ValidationException;
import java.util.List;

@Service
@Slf4j
public class ReviewService {
    private final ReviewDao reviewDao;

    public ReviewService(ReviewDao reviewDao) {
        this.reviewDao = reviewDao;
    }

    public List<Review> findAll(Integer filmId, Integer count) {
        return reviewDao.findAll(filmId, count);
    }

    public Review create(Review review) {
        validReview(review);
        System.out.println("Ну и почему мы продолжаем");
        return reviewDao.create(review);
    }

    public Review update(Review review) {
        validReview(review);
        System.out.println("Ну и почему мы продолжаем");
        return reviewDao.update(review);
    }

    public void delete(Integer id) {
        reviewDao.removeReviewById(id);
    }

    public void addLikeReview(Integer reviewId, Integer userId) {
        reviewDao.addLikeReview(reviewId, userId);

    }

    public void addDislikeReview(Integer reviewId, Integer userId) {
        reviewDao.addDislikeReview(reviewId, userId);
    }

    public void removeLikeReview(Integer reviewId, Integer userId) {
        reviewDao.removeLikeReview(reviewId, userId);
    }

    public void removeDislikeReview(Integer reviewId, Integer userId) {
        reviewDao.removeDislikeReview(reviewId, userId);
    }

    public Review getReviewById(Integer id) {
        log.info("Получение отзыва с id {}", id);
        return reviewDao.getById(id);
    }

    public void validReview(Review review) {
        if (review.getContent() == null || review.getContent().isEmpty() || review.getContent().isBlank()) {
            log.warn("Попытка создания пустого отзыва");
            throw new ValidationException("Отзыв не может быть пустым");
        } else if (review.getIsPositive() == null) {
            log.warn("Попытка создания бесполезного отзыва");
            throw new ValidationException("Отзыв должен быть либо положительным либо негативным");
        } else if (review.getFilmId() < 0 || review.getFilmId() == null) {
            log.warn("Попытка добавить отзыв к несуществующему фильму");
            throw new FilmNotFoundException("Нельзя добавить отзыв к несуществующему фильму");
        } else if (review.getUserId() < 0||review.getUserId()==null) {
            log.warn("Пользователя с таким id не существует");
            throw new UserNotFoundException("Несуществующий пользователь не может добавлять отзывы");
        }

    }

}
