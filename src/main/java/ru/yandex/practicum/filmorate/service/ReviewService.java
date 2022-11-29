package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.dao.ReviewDao;

import java.util.List;

import static ru.yandex.practicum.filmorate.model.enums.EventType.REVIEW;
import static ru.yandex.practicum.filmorate.model.enums.Operation.REMOVE;
import static ru.yandex.practicum.filmorate.model.enums.Operation.UPDATE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewDao reviewDao;
    private final FeedService feedService;

    public List<Review> findAll(Integer filmId, Integer count) {
        return reviewDao.findAll(filmId, count);
    }

    public Review add(Review review) {
        throwIfReviewNotValid(review);

        Review rw = reviewDao.add(review);
        feedService.add(rw.getReviewId(), rw.getUserId(), REVIEW, Operation.ADD);
        return rw;
    }

    public Review update(Review review) {
        throwIfReviewNotValid(review);

        Review rw = reviewDao.findById(review.getReviewId()).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        feedService.add(rw.getReviewId(), rw.getUserId(), REVIEW, UPDATE);
        return reviewDao.update(review);
    }

    public void delete(Integer id) {
        Review rw = reviewDao.findById(id).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        reviewDao.delete(id);

        feedService.add(rw.getReviewId(), rw.getUserId(), REVIEW, REMOVE);
    }

    public void addLikeReview(Integer reviewId, Integer userId) {
        reviewDao.addLike(reviewId, userId);
    }

    public void addDislike(Integer reviewId, Integer userId) {
        reviewDao.addDislike(reviewId, userId);
    }

    public void deleteLike(Integer reviewId, Integer userId) {
        reviewDao.deleteLike(reviewId, userId);
    }

    public void deleteDislike(Integer reviewId, Integer userId) {
        reviewDao.deleteDislike(reviewId, userId);
    }

    public Review findById(Integer id) {
        log.info("Получение отзыва с id {}", id);
        return reviewDao.findById(id).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    public void throwIfReviewNotValid(Review review) {
        if (review.getFilmId() < 0 || review.getFilmId() == null) {
            log.warn("Попытка добавить отзыв к несуществующему фильму");
            throw new NotFoundException("Нельзя добавить отзыв к несуществующему фильму");
        } else if (review.getUserId() < 0 || review.getUserId() == null) {
            log.warn("Пользователя с таким id не существует");
            throw new NotFoundException("Несуществующий пользователь не может добавлять отзывы");
        }
    }
}
