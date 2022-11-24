package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

@Data
@Builder
public class Feed {
    private long eventId;
    private int entityId;
    private int userId;
    private long timestamp;
    private EventType eventType;
    private Operation operation;
}
