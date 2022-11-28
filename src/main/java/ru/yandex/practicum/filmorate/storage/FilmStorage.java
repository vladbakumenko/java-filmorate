package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Collection<Film> findAll();

    Film getById(Integer id);

    List<Film> findByDirectorIdSortedByYear(Integer directorId);

    List<Film> findByDirectorIdSortedByLikes(Integer directorId);

    Film create(Film film);

    Film update(Film film);

    Collection<Film> searchFilms(String query, String groupBy);

    void deleteById(Integer id);
}
