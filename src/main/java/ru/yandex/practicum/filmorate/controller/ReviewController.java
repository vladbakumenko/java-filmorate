package ru.yandex.practicum.filmorate.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Component
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<Review> findAll(@RequestParam(required = false) Integer filmId, @RequestParam(required = false, defaultValue = "10") Integer count) {
        return reviewService.findAll(filmId, count);
    }

    @PostMapping
    public Review create(@RequestBody Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@RequestBody Review review) {
        return reviewService.update(review);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Integer id) {
        return reviewService.getReviewById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeReview(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.addLikeReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeReview(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.addDislikeReview(id, userId);

    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLikeReview(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.removeLikeReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislikeReview(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.removeDislikeReview(id, userId);
    }

    @DeleteMapping("/{id}")
    public void removeReviewById(@PathVariable("id") Integer id) {
        reviewService.delete(id);
    }

}
