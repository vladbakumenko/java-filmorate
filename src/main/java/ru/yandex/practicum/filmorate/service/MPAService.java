package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.dao.MPADao;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MPAService {

    private final MPADao mpaDao;

    public Collection<MPA> findAll() {
        return mpaDao.findAll();
    }

    public MPA getMpaById(int idMPA) {
        return mpaDao.getMpaById(idMPA);
    }
}
