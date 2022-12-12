package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Optional;

import ru.yandex.practicum.filmorate.model.Director;

public interface DirectorStorage {

    List<Director> findAll();

    Optional<Director> findById(Integer id);

    Director add(Director director);

    Director update(Director director);

    void delete(Integer id);
}
