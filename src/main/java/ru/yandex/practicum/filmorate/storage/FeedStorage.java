package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.util.List;

public interface FeedStorage {

    List<Feed> getByUserId(int id);

    void addFeed(int idEntity, int idUser, long timestamp, EventType eventType, Operation operation);
}
