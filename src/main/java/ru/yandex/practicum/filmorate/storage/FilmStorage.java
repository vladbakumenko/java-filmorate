package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> findAll();

    Optional<Film> findById(Integer id);

    List<Film> findByDirectorIdSortedByYear(Integer directorId);

    List<Film> findByDirectorIdSortedByLikes(Integer directorId);

    Film create(Film film);

    Film update(Film film);

    List<Film> searchFilms(String query, String groupBy);

    void deleteById(Integer id);

    List<Film> getCommonFilms(Integer userId, Integer friendId);
}
