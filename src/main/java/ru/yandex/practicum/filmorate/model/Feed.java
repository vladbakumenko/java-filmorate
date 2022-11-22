package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.time.Instant;

@Data
@Builder
public class Feed {
    private long idEvent;
    private int idEntity;
    private int idUser;
    private long timestamp;
    private EventType eventType;
    private Operation operation;
}
