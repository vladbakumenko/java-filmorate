package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    List<Review> findAll(Integer filmId,Integer count);
    Review getById(Integer id);
    Review create(Review review);
    Review update(Review review);
    void removeReviewById(Integer id);


}
