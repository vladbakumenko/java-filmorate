package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.LikesDao;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    private void validFilm(Film film) {
        if (film.getName() == null || film.getName().isEmpty() || film.getName().isBlank()) {
            log.warn("Попытка создания фильма с пустым названием");
            throw new ValidationException("Название фильма не может быть пустым");
        } else if (film.getDescription().length() > 200) {
            log.warn("Попытка создания фильма с описанием свыше 200 знаков");
            throw new ValidationException("Описание фильма превышает максимальное количество знаков 200");
        } else if (film.getReleaseDate().isBefore(firstFilmBirthday)) {
            log.warn("Попытка создания фильма с датой, предшествующей появлению первого фильма");
            throw new ValidationException("Дата релиза фильма введена неверна");
        } else if (film.getDuration() <= 0) {
            log.warn("Попытка создания фильма с отрицательной продолжительностью");
            throw new ValidationException("Продолжительность фильма не может быть отрицательной");
        }
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validFilm(film);
        return filmStorage.update(film);
    }

    public Film getById(Integer id) {
        return filmStorage.getById(id);
    }


    public void addLikeForDb(Integer id, Integer userId) {
        likesDao.addLike(id, userId);

        feedService.addFeed(id, userId, EventType.LIKE, Operation.ADD);
    }

    public void removeLikeFromDb(Integer id, Integer userId) {
        likesDao.removeLike(id, userId);

        feedService.addFeed(id, userId, EventType.LIKE, Operation.REMOVE);
    }

    public List<Film> findPopular(Integer count, Optional<Integer> genreId, Optional<Integer> year) {
        return likesDao.getPopular(count, genreId, year);
    }

    public List<Film> findByDirectorId(Integer directorId, String sortParam) {
        List<Film> films;

        directorService.findById(directorId);

        if (sortParam.equals("year")) {
            films = filmStorage.findByDirectorIdSortedByYear(directorId);
        } else if (sortParam.equals("likes")) {
            films = filmStorage.findByDirectorIdSortedByLikes(directorId);
        } else throw new ValidationException(format("Incorrect parameters value: %s", sortParam));

        return films;
    }

    public Collection<Film> searchFromDb(String query, String groupBy) {
        return filmStorage.searchFilms(query, groupBy);
    }

    public void deleteById(Integer id) {
        filmStorage.deleteById(id);
    }
}
