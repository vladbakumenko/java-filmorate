package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public List<Review> findAll(@RequestParam(required = false) Integer filmId,
                                @RequestParam(required = false, defaultValue = "10") Integer count) {
        return reviewService.findAll(filmId, count);
    }

    @PostMapping
    public Review add(@RequestBody @Valid Review review) {
        return reviewService.add(review);
    }

    @PutMapping
    public Review update(@RequestBody @Valid Review review) {
        return reviewService.update(review);
    }

    @GetMapping("/{id}")
    public Review findById(@PathVariable Integer id) {
        return reviewService.findById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.addLikeReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeReview(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        reviewService.deleteDislike(id, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") Integer id) {
        reviewService.delete(id);
    }
}
