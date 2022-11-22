package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;

import ru.yandex.practicum.filmorate.model.Director;

public interface DirectorStorage {

    Collection<Director> findAll();

    Director getById(Integer id);

    Director create(Director director);

    Director update(Director director);

    void delete(Integer id);
}
