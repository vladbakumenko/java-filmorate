package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.dto.FilmAndGenresDto;
import ru.yandex.practicum.filmorate.storage.dao.GenreDao;

import java.util.ArrayList;
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

    public List<Film> saveGenresForFilms(List<Film> films) {
        Map<Integer, Genre> map = new HashMap<>();

        for (FilmAndGenresDto dto : genreDao.findFilmIdAndItsListOfIdGenre(films)) {
            map.put(dto.getFilmId(), new Genre(dto.getGenreId(), dto.getNameGenre()));
        }

        for (Film film : films) {
            int filmId = film.getId();
            List<Genre> genres = new ArrayList<>();

            for (Integer id : map.keySet()) {
                if (id == filmId) {
                    genres.add(map.get(id));
                }
            }
            film.setGenres(genres);
        }

        return films;
    }
}
