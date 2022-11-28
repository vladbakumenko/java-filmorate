package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.FilmsDao;
import ru.yandex.practicum.filmorate.storage.dao.ReviewDao;
import ru.yandex.practicum.filmorate.storage.dao.UsersDao;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReviewTests {
    private final ReviewDao reviewDao;
    private final UsersDao usersDao;
    private final FilmsDao filmsDao;

    @BeforeAll
    @Test
    public void createUserAndFilm() {
        usersDao.create(User.builder().id(1).name("user").email("test@mail.ru").
                birthday(LocalDate.of(1980, 12, 7)).build());
        filmsDao.create(Film.builder().
                id(1).description("description").
                name("film").duration(120).
                releaseDate(LocalDate.of(2001, 8, 1)).
                mpa(new Mpa(1, "mpa", "mpa")).
                build());
    }

    @Test
    @Order(1)
    public void testCreateReview() {
        Review review = Review.builder().
                content("test content").isPositive(true).
                userId(1).
                filmId(1)
                .useful(0).build();
        reviewDao.create(review);
        assertThat(review).hasFieldOrPropertyWithValue("reviewId", 1);
    }

    @Test
    @Order(2)
    public void testGetReview() {
        Review review = reviewDao.getById(1);
        assertThat(review).
                hasFieldOrPropertyWithValue("reviewId", 1).
                hasFieldOrPropertyWithValue("content", "test content");
    }

    @Test
    @Order(3)
    public void testUpdateReview() {
        Review updateReview = Review.builder().reviewId(1).content("content").isPositive(false).useful(1).build();
        reviewDao.update(updateReview);
        assertThat(updateReview).hasFieldOrPropertyWithValue("reviewId", 1);

    }

    @Test
    @Order(5)
    public void testDeleteReview() {
        reviewDao.removeReviewById(1);
        Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
            reviewDao.getById(1);
        });
        assertThat(exception.getMessage()).contains("Review with id: " + 1 + " not found");
    }

}
