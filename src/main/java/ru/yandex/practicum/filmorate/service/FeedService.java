package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.dao.FeedDao;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedDao feedDao;

    public void addFeed(int idEntity, int idUser, EventType eventType, Operation operation) {
        feedDao.addFeed(idEntity, idUser, eventType, operation);
    }

    public Collection<Feed> getFeedByUserId(int id) {
        return feedDao.getFeedByUserId(id);
    }
}
