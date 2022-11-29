package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.MpaDao;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaDao mpaDao;

    public List<Mpa> findAll() {
        return mpaDao.findAll();
    }

    public Mpa getById(int idMPA) {
        Optional<Mpa> optionalMpa = mpaDao.findMpaById(idMPA);

        if (optionalMpa.isEmpty()) {
            throw new NotFoundException(String.format("MPA with id: %d not found", idMPA));
        }

        return optionalMpa.get();
    }
}
