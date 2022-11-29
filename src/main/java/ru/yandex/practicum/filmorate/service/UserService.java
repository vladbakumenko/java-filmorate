package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.FriendsDao;
import ru.yandex.practicum.filmorate.storage.dao.LikesDao;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static ru.yandex.practicum.filmorate.model.enums.EventType.FRIEND;
import static ru.yandex.practicum.filmorate.model.enums.Operation.ADD;
import static ru.yandex.practicum.filmorate.model.enums.Operation.REMOVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FriendsDao friendsDao;
    private final FeedService feedService;
    private final LikesDao likesDao;

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        validUser(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validUser(user);
        return userStorage.update(user);
    }

    public User getById(Integer id) {
        return userStorage.getById(id);
    }

    public void addFriend(Integer id, Integer friendId) {
        friendsDao.addFriend(id, friendId);

        feedService.add(friendId, id, FRIEND, ADD);
    }

    public void removeFriend(Integer id, Integer friendId) {
        friendsDao.removeFriend(id, friendId);

        feedService.add(friendId, id, FRIEND, REMOVE);
    }

    public Collection<User> getFriends(Integer id) {
        return friendsDao.getFriends(id);
    }

    public Collection<User> getCommonFriends(Integer id, Integer otherId) {
        return friendsDao.getCommonFriends(id, otherId);
    }

    public void deleteById(Integer id) {
        userStorage.deleteById(id);
    }

    public List<Film> getRecommendedFilm(Integer id) {
        Integer recommendedId = likesDao.getUsersWithMaximumIntersectionLikes(id);
        return likesDao.getRecommendedFilm(id, recommendedId);
    }

    public void checkUserExist(Integer id) {
        userStorage.checkUserExist(id);
    }

    private void validUser(User user) {
        LocalDate now = LocalDate.now();

        if (user.getEmail() == null || user.getEmail().isEmpty() || user.getEmail().isBlank()
                || !user.getEmail().contains("@")) {
            log.warn("Попытка создать пользователя с пустым или не корректным адресом email");
            throw new BadRequestException("Адрес почты введён неверно");
        } else if (user.getLogin() == null || user.getLogin().isEmpty() || user.getLogin().isBlank()
                || user.getLogin().contains(" ")) {
            log.warn("Попытка создать пользователя с пустым или содержащим пробелы логином");
            throw new BadRequestException("Логин не должен быть пустым и содержать пробелы");
        } else if (user.getBirthday().isAfter(now)) {
            log.warn("Попытка создать пользователя с датой рождения из будущего");
            throw new BadRequestException("День рождения не может быть из будущего :)");
        }

        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            log.warn("Попытка создать пользователя с пустым именем, вместо имени будет присвоен логин");
            user.setName(user.getLogin());
        }
    }

    public List<Feed> getFeedByUserId(Integer id) {
        checkUserExist(id);
        return feedService.getByUserId(id);
    }
}
