package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Collection<Film> getSorted(Integer directorId, String sortParam);

    Film create(Film film);

    Film update(Film film);

    Film getById(Integer id);

    Collection<Film> searchFilms(String query, String groupBy);

    void deleteById(Integer id);

    Collection<Film> getCommonFilms(Integer userId, Integer friendId);
}
