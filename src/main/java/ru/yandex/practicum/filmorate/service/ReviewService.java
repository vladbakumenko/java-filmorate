package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.dao.ReviewDao;

import java.util.List;

@Service
@Slf4j
public class ReviewService {
    private final ReviewDao reviewDao;
    private final FeedService feedService;

    public ReviewService(ReviewDao reviewDao, FeedService feedService) {
        this.reviewDao = reviewDao;
        this.feedService = feedService;
    }

    public List<Review> findAll(Integer filmId, Integer count) {
        return reviewDao.findAll(filmId, count);
    }

    public Review create(Review review) {
        validateReview(review);

        Review rw = reviewDao.create(review);
        feedService.addFeed(rw.getReviewId(), rw.getUserId(), EventType.REVIEW, Operation.ADD);
        return rw;
    }

    public Review update(Review review) {
        validateReview(review);

        Review rw = reviewDao.getById(review.getReviewId());
        feedService.addFeed(rw.getReviewId(), rw.getUserId(), EventType.REVIEW, Operation.UPDATE);
        return reviewDao.update(review);
    }

    public void delete(Integer id) {
        Review rw = reviewDao.getById(id);
        reviewDao.removeReviewById(id);

        feedService.addFeed(rw.getReviewId(), rw.getUserId(), EventType.REVIEW, Operation.REMOVE);
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

    public void validateReview(Review review) {
        if (review.getFilmId() < 0 || review.getFilmId() == null) {
            log.warn("Попытка добавить отзыв к несуществующему фильму");
            throw new FilmNotFoundException("Нельзя добавить отзыв к несуществующему фильму");
        } else if (review.getUserId() < 0 || review.getUserId() == null) {
            log.warn("Пользователя с таким id не существует");
            throw new UserNotFoundException("Несуществующий пользователь не может добавлять отзывы");
        }

    }

}
