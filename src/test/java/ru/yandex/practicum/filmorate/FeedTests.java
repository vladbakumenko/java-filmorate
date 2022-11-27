package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeedTests {

    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    @Test
    @Order(1)
    public void testAddFeedAndGetFeedByUserId() {
        User user = User.builder()
                .email("email@email.ru")
                .login("login")
                .name("name")
                .birthday(LocalDate.now().minusYears(100))
                .build();
        userStorage.create(user);

        Feed feed = Feed.builder()
                .userId(1)
                .entityId(1)
                .timestamp(Instant.now().toEpochMilli())
                .eventType(EventType.FRIEND)
                .operation(Operation.ADD)
                .build();

        feedStorage.addFeed(
                feed.getEntityId(),
                feed.getUserId(),
                feed.getTimestamp(),
                feed.getEventType(),
                feed.getOperation()
        );

        feed.setEventId(1);

        assertTrue(feedStorage.getByUserId(1).contains(feed));
    }

    @Test
    @Order(2)
    public void testGetFeedByWrongId() {
        UserNotFoundException e = assertThrows(UserNotFoundException.class, () -> {
            feedStorage.getByUserId(10);
        });

        assertEquals(e.getMessage(), "User with id: 10 not found in DB");
    }

    @Test
    @Order(3)
    public void testGetFeedByIdWithoutEvents() {
        User user = User.builder()
                .email("email2@email.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.now().minusYears(100))
                .build();
        userStorage.create(user);

        assertTrue(feedStorage.getByUserId(2).isEmpty());
    }
}
