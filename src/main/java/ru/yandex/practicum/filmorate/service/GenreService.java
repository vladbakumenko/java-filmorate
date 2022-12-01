package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreDao;

import java.util.List;

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
}
