package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.LikesDao;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

import static ru.yandex.practicum.filmorate.model.enums.EventType.LIKE;
import static ru.yandex.practicum.filmorate.model.enums.Operation.ADD;
import static ru.yandex.practicum.filmorate.model.enums.Operation.REMOVE;

import static java.lang.String.format;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final LocalDate firstFilmBirthday = LocalDate.of(1895, Month.DECEMBER, 28);
    private final FilmStorage filmStorage;
    private final LikesDao likesDao;
    private final FeedService feedService;
    private final DirectorService directorService;
    private final UserService userService;

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        throwIfFilmNotValid(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        getById(film.getId());
        throwIfFilmNotValid(film);
        return filmStorage.update(film);
    }

    public Film getById(Integer id) {
        Optional<Film> optionalFilm = filmStorage.findById(id);

        if (optionalFilm.isEmpty()) {
            throw new NotFoundException(String.format("Film with id: %d not found", id));
        }

        return optionalFilm.get();
    }


    public void addLike(Integer id, Integer userId) {
        userService.checkUserExist(userId);
        likesDao.addLike(id, userId);

        feedService.add(id, userId, LIKE, ADD);
    }

    public void removeLike(Integer id, Integer userId) {
        userService.checkUserExist(userId);
        likesDao.removeLike(id, userId);

        feedService.add(id, userId, LIKE, REMOVE);
    }

    public List<Film> getPopular(Integer count, Optional<Integer> genreId, Optional<Integer> year) {
        return likesDao.getPopular(count, genreId, year);
    }

    public List<Film> getByDirectorId(Integer directorId, String sortParam) {
        List<Film> films;

        directorService.getById(directorId);

        if (sortParam.equals("year")) {
            films = filmStorage.findByDirectorIdSortedByYear(directorId);
        } else if (sortParam.equals("likes")) {
            films = filmStorage.findByDirectorIdSortedByLikes(directorId);
        } else throw new BadRequestException(format("Incorrect parameters value: %s", sortParam));

        return films;
    }

    public List<Film> search(String query, String groupBy) {
        switch (groupBy) {
            case "title":
                return filmStorage.searchByTitle(query);
            case "director":
                return filmStorage.searchByDirector(query);
            case "director,title":
            case "title,director":
                return filmStorage.searchByTitleAndDirector(query);
            default:
                throw new BadRequestException("Incorrect parameters value");
        }
    }

    public void deleteById(Integer id) {
        getById(id);
        filmStorage.deleteById(id);
    }

    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        userService.checkUserExist(userId);
        userService.checkUserExist(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }

    private void throwIfFilmNotValid(Film film) {
        if (film.getName() == null || film.getName().isEmpty() || film.getName().isBlank()) {
            throw new BadRequestException("Название фильма не может быть пустым");
        } else if (film.getDescription().length() > 200) {
            throw new BadRequestException("Описание фильма превышает максимальное количество знаков 200");
        } else if (film.getReleaseDate().isBefore(firstFilmBirthday)) {
            throw new BadRequestException("Дата релиза фильма введена неверна");
        } else if (film.getDuration() <= 0) {
            throw new BadRequestException("Продолжительность фильма не может быть отрицательной");
        }
    }
}
