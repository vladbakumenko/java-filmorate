package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.dto.FilmAndGenresDto;
import ru.yandex.practicum.filmorate.storage.dao.GenreDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreDao genreDao;

    public Genre getById(int idGenre) {
        return genreDao.findGenreById(idGenre)
                .orElseThrow(() -> new NotFoundException(String.format("Genre with id: %d not found", idGenre)));
    }

    public List<Genre> findAll() {
        return genreDao.findAll();
    }

    public Map<Integer, Genre> getMapOfFilmsIdAndGenres() {
        Map<Integer, Genre> map = new HashMap<>();

        for (FilmAndGenresDto dto : genreDao.findFilmIdAndItsListOfIdGenre()) {
            map.put(dto.getFilmId(), new Genre(dto.getGenreId(), dto.getNameGenre()));
        }

        return map;
    }
}
