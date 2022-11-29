package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    List<Review> findAll(Integer filmId, Integer count);

    Optional<Review> findById(Integer id);

    Review add(Review review);

    Review update(Review review);

    void delete(Integer id);
}
