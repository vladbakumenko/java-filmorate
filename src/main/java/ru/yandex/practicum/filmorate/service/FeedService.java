package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.dao.FeedDao;

import java.time.Instant;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedStorage feedStorage;

    public void addFeed(int idEntity, int idUser, EventType eventType, Operation operation) {
        long timestamp = Instant.now().toEpochMilli();
        feedStorage.addFeed(idEntity, idUser, timestamp, eventType, operation);
    }

    public Collection<Feed> getFeedByUserId(int id) {
        return feedStorage.getFeedByUserId(id);
    }
}
