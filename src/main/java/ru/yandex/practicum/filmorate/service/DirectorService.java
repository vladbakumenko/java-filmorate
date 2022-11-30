package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public List<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director getById(Integer id) {
        Optional<Director> director = directorStorage.findById(id);

        if (director.isEmpty()) {
            throw new NotFoundException(format("Director with id: %d not found in DB", id));
        }
        return director.get();
    }

    public Director add(Director director) {
        return directorStorage.add(director);
    }

    public Director update(Director director) {
        getById(director.getId());

        return directorStorage.update(director);
    }

    public void delete(Integer id) {
        getById(id);

        directorStorage.delete(id);
    }
}
