package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.LikesDao;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.model.enums.EventType.LIKE;
import static ru.yandex.practicum.filmorate.model.enums.Operation.ADD;
import static ru.yandex.practicum.filmorate.model.enums.Operation.REMOVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final LocalDate firstFilmBirthday = LocalDate.of(1895, Month.DECEMBER, 28);
    private final FilmStorage filmStorage;
    private final LikesDao likesDao;
    private final FeedService feedService;

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


    public void addLike(Integer id, Integer userId) {
        likesDao.addLike(id, userId);

        feedService.add(id, userId, LIKE, ADD);
    }

    public void removeLike(Integer id, Integer userId) {
        likesDao.removeLike(id, userId);

        feedService.add(id, userId, LIKE, REMOVE);
    }

    public Collection<Film> getPopular(Integer count, Optional<Integer> genreId, Optional<Integer> year) {
        return likesDao.getPopular(count, genreId, year);
    }

    public Collection<Film> getFilmsByDirector(Integer directorId, String sortParam) {
        return filmStorage.getSorted(directorId, sortParam);
    }

    public Collection<Film> search(String query, String groupBy) {
        return filmStorage.searchFilms(query, groupBy);
    }

    public void deleteById(Integer id) {
        filmStorage.deleteById(id);
    }

    public Collection<Film> getCommonFilms(Integer userId, Integer friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

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
}
