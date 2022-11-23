package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.Collection;

@Service
public class DirectorService {

    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(@Qualifier("directorDao") DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public Collection<Director> findAll() {
        return directorStorage.findAll();
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        return directorStorage.update(director);
    }

    public Director getById(Integer id) {
        return directorStorage.getById(id);
    }

    public void delete(Integer id) {
        directorStorage.delete(id);
    }
}
