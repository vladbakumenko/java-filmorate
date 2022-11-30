package ru.yandex.practicum.filmorate;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.service.MpaService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.dao.*;
import ru.yandex.practicum.filmorate.storage.dao.MpaDao;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmoRateApplicationTests {
    private final DirectorStorage directorStorage;
    private final UsersDao userStorage;
    private final UserService userService;
    private final FilmsDao filmStorage;
    private final FilmService filmService;
    private final FriendsDao friendsDao;
    private final GenreDao genreDao;
    private final GenreService genreService;
    private final LikesDao likesDao;
    private final MpaDao mpaDao;
    private final MpaService mpaService;
    private final JdbcTemplate jdbcTemplate;
    private User user;
    private Film film;
    private Director director;

    @BeforeEach
    public void addDataAndRestartDb() {
        jdbcTemplate.update("DELETE FROM likes_by_users");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM film_directors");
        jdbcTemplate.update("DELETE FROM friends");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");

        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");

        user = User.builder()
                .email("email@email.ru")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 10))
                .build();

        userStorage.create(user);

        director = Director.builder()
                .name("Director")
                .build();

        director = directorStorage.add(director).orElseThrow();

        film = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(2010, Month.DECEMBER, 10))
                .duration(100)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(director))
                .build();

        filmStorage.create(film);
    }

    @Test
    public void testFindUserById() {
        User testUser = userStorage.findById(1);

        assertThat(testUser).hasFieldOrPropertyWithValue("id", 1);

        assertEquals(user, testUser);
    }

    @Test
    public void testFindUserWithWrongId() {
        NotFoundException e = assertThrows(NotFoundException.class, () -> {
            userStorage.findById(100);
        });

        assertEquals(e.getMessage(), "User with id: 100 not found in DB");
    }

    @Test
    public void testFindAllUsers() {
        Collection<User> users = userStorage.findAll();

        assertEquals(1, users.size());
        assertTrue(users.contains(user));
    }

    @Test
    public void testCreateUser() {
        User userTest = User.builder()
                .email("email2@email.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 20))
                .build();

        userStorage.create(userTest);

        assertThat(userTest).hasFieldOrPropertyWithValue("id", 2);

        assertEquals(userTest, userStorage.findById(2));
    }

    @Test
    public void testCreateUserWithSomeId() {
        User userTest = User.builder()
                .id(100)
                .email("email2@email.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 20))
                .build();

        userStorage.create(userTest);

        assertThat(userTest).hasFieldOrPropertyWithValue("id", 2);

        assertEquals(userTest, userStorage.findById(2));
    }

    @Test
    public void testUpdateUser() {
        User userTest = User.builder()
                .id(1)
                .email("email2@email.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 20))
                .build();

        userStorage.update(userTest);

        assertThat(userTest).hasFieldOrPropertyWithValue("id", 1);

        assertEquals(userTest, userStorage.findById(1));
    }

    @Test
    public void testUpdateUserWithWrongId() {
        User userTest = User.builder()
                .id(100)
                .email("email2@email.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 20))
                .build();

        NotFoundException e = assertThrows(NotFoundException.class, () -> {
            userService.update(userTest);
        });

        assertEquals(e.getMessage(), "User with id: 100 not found");
    }

    @Test
    public void testFindFilmById() {
        Optional<Film> optionalFilm = filmStorage.findById(1);

        assertThat(optionalFilm)
                .isPresent()
                .hasValueSatisfying(film -> assertThat(film).hasFieldOrPropertyWithValue("id", 1));

        assertEquals(film, optionalFilm.orElseThrow());
    }

    @Test
    public void testFindFilmWithWrongId() {
        NotFoundException e = assertThrows(NotFoundException.class, () -> {
            filmService.getById(100);
        });

        assertEquals(e.getMessage(), "Film with id: 100 not found");
    }

    @Test
    public void testFindAllFilms() {
        Collection<Film> films = filmStorage.findAll();

        assertEquals(1, films.size());
        assertTrue(films.contains(film));
    }

    @Test
    public void testCreateFilm() {
        Film testFilm = Film.builder()
                .name("film2")
                .description("description2")
                .releaseDate(LocalDate.of(2010, Month.DECEMBER, 20))
                .duration(200)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(director))
                .build();

        filmStorage.create(testFilm);

        assertThat(testFilm).hasFieldOrPropertyWithValue("id", 2);

        assertEquals(testFilm, filmStorage.findById(2).orElseThrow());
    }

    @Test
    public void testCreateFilmWithSomeId() {
        Film testFilm = Film.builder()
                .id(100)
                .name("film2")
                .description("description2")
                .releaseDate(LocalDate.of(2010, Month.DECEMBER, 20))
                .duration(200)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(director))
                .build();

        filmStorage.create(testFilm);

        assertThat(testFilm).hasFieldOrPropertyWithValue("id", 2);

        assertEquals(testFilm, filmStorage.findById(2).orElseThrow());
    }

    @Test
    public void testUpdateFilm() {
        Film testFilm = Film.builder()
                .id(1)
                .name("film2")
                .description("description2")
                .releaseDate(LocalDate.of(2010, Month.DECEMBER, 20))
                .duration(200)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(director))
                .build();

        filmStorage.update(testFilm);

        assertThat(testFilm).hasFieldOrPropertyWithValue("id", 1);

        assertEquals(testFilm, filmStorage.findById(1).orElseThrow());
    }

    @Test
    public void testUpdateFilmWithWrongId() {
        Film testFilm = Film.builder()
                .id(100)
                .name("film2")
                .description("description2")
                .releaseDate(LocalDate.of(2010, Month.DECEMBER, 20))
                .duration(200)
                .mpa(new Mpa(2, "mpa2", "mpa-desc2"))
                .genres(List.of(new Genre(2, "genre2")))
                .build();

        NotFoundException e = assertThrows(NotFoundException.class, () -> {
            filmService.update(testFilm);
        });

        assertEquals(e.getMessage(), "Film with id: 100 not found");
    }

    @Test
    public void testAddFriend() {
        User userTest = User.builder()
                .email("email2@email.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 20))
                .build();

        userStorage.create(userTest);

        friendsDao.addFriend(user.getId(), userTest.getId());

        assertTrue(friendsDao.findFriends(user.getId()).contains(userTest));
    }

    @Test
    public void testAddFriendWithWrongId() {
        NotFoundException e = assertThrows(NotFoundException.class, () -> {
            userService.addFriend(1, 100);
        });

        assertEquals(e.getMessage(), "User with id: 100 not found");
    }

    @Test
    public void testRemoveFriend() {
        User userTest = User.builder()
                .email("email2@email.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 20))
                .build();

        userStorage.create(userTest);

        friendsDao.addFriend(user.getId(), userTest.getId());

        friendsDao.removeFriend(user.getId(), userTest.getId());

        assertFalse(friendsDao.findFriends(user.getId()).contains(userTest));
    }

    @Test
    public void testGetFriends() {
        User userTest = User.builder()
                .email("email2@email.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 20))
                .build();

        userStorage.create(userTest);

        friendsDao.addFriend(user.getId(), userTest.getId());

        assertEquals(friendsDao.findFriends(user.getId()), List.of(userTest));
    }

    @Test
    public void testGetCommonFriends() {
        User userTest = User.builder()
                .email("email2@email.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 20))
                .build();

        userStorage.create(userTest);

        friendsDao.addFriend(user.getId(), userTest.getId());

        User userTest2 = User.builder()
                .email("email3@email.ru")
                .login("login3")
                .name("name3")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 20))
                .build();

        userStorage.create(userTest2);

        friendsDao.addFriend(userTest2.getId(), userTest.getId());

        assertEquals(friendsDao.findCommonFriends(user.getId(), userTest2.getId()), List.of(userTest));
    }

    @Test
    public void testGetGenreById() {
        Optional<Genre> optionalGenre = genreDao.findGenreById(1);

        assertThat(optionalGenre)
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre).hasFieldOrPropertyWithValue("id", 1));

        assertThat(optionalGenre)
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre)
                        .hasFieldOrPropertyWithValue("name", "Комедия"));
    }

    @Test
    public void testGetGenreByIdWithWrongId() {
        NotFoundException e = assertThrows(NotFoundException.class, () -> {
            genreService.getById(10);
        });

        assertEquals(e.getMessage(), "Genre with id: 10 not found");
    }

    @Test
    public void testGetAllGenres() {
        assertEquals(genreDao.findAll().size(), 6);
    }

    @Test
    public void testAddLike() {
        likesDao.addLike(film.getId(), user.getId());

        Boolean bool = jdbcTemplate.queryForObject("select exists " +
                        "(select * from likes_by_users where id_film = ? and id_user = ?)",
                Boolean.class, film.getId(), user.getId());

        assertTrue(bool);
    }

    @Test
    public void testRemoveLike() {
        likesDao.addLike(film.getId(), user.getId());
        likesDao.removeLike(film.getId(), user.getId());

        Boolean bool = jdbcTemplate.queryForObject("select exists " +
                        "(select * from likes_by_users where id_film = ? and id_user = ?)",
                Boolean.class, film.getId(), user.getId());

        assertFalse(bool);
    }

    @Test
    public void testGetPopular() {
        Film testFilm = Film.builder()
                .name("film2")
                .description("description2")
                .releaseDate(LocalDate.of(2010, Month.DECEMBER, 20))
                .duration(200)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(director))
                .build();

        filmStorage.create(testFilm);

        Film testFilm2 = Film.builder()
                .name("film3")
                .description("description3")
                .releaseDate(LocalDate.of(2010, Month.DECEMBER, 20))
                .duration(300)
                .mpa(new Mpa(2, null, null))
                .genres(List.of(new Genre(2, null)))
                .directors(List.of(director))
                .build();

        filmStorage.create(testFilm2);

        User userTest = User.builder()
                .email("email2@email.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 20))
                .build();

        userStorage.create(userTest);

        likesDao.addLike(film.getId(), user.getId());
        likesDao.addLike(film.getId(), userTest.getId());

        likesDao.addLike(testFilm.getId(), user.getId());

        assertEquals(likesDao.getPopular(10, Optional.empty(), Optional.empty()),
                List.of(film, testFilm, testFilm2));
        assertEquals(likesDao.getPopular(10, Optional.of(1), Optional.of(2010)),
                List.of(film, testFilm));
        assertEquals(likesDao.getPopular(10, Optional.of(2), Optional.of(2010)),
                List.of(testFilm2));
        assertEquals(likesDao.getPopular(10, Optional.empty(), Optional.of(2010)),
                List.of(film, testFilm, testFilm2));
    }

    @Test
    public void testGetMpaById() {
        Optional<Mpa> optionalMpa = mpaDao.findMpaById(1);

        assertThat(optionalMpa)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("id", 1));

        assertThat(optionalMpa)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("name", "G"));
    }

    @Test
    public void testGetMpaByIdWithWrongId() {
        NotFoundException e = assertThrows(NotFoundException.class, () -> {
            mpaService.getById(10);
        });

        assertEquals(e.getMessage(), "MPA with id: 10 not found");
    }

    @Test
    public void testGetAllMpa() {
        assertEquals(mpaDao.findAll().size(), 5);
    }


    @Test
    public void testSearchByTitle() {
        Film testFilm = Film.builder()
                .name("filmsEaRch")
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.JANUARY, 1))
                .duration(100)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(director))
                .build();

        testFilm = filmStorage.create(testFilm);

        assertEquals(List.of(testFilm), filmStorage.searchByTitle("search"));
    }

    @Test
    public void testSearchByDirector() {
        Director searchDirector = Director.builder()
                .name("DirectorForSSSSSEaRCH")
                .build();

        searchDirector = directorStorage.add(searchDirector).orElseThrow();
        Film testFilm = Film.builder()
                .name("filmForFind")
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.JANUARY, 1))
                .duration(100)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(searchDirector))
                .build();
        testFilm = filmStorage.create(testFilm);

        assertEquals(List.of(testFilm), filmStorage.searchByDirector("search"));
    }

    @Test
    public void testSearchByDirectorAndTitle() {
        Film testFilm = Film.builder()
                .name("filmsEaRch")
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.JANUARY, 1))
                .duration(100)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(director))
                .build();

        testFilm = filmStorage.create(testFilm);

        Director searchDirector = Director.builder()
                .name("DirectorForSSSSSEaRCH")
                .build();

        searchDirector = directorStorage.add(searchDirector).orElseThrow();
        Film testFilm2 = Film.builder()
                .name("filmForFind")
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.JANUARY, 1))
                .duration(100)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(searchDirector))
                .build();
        testFilm2 = filmStorage.create(testFilm2);

        assertEquals(List.of(testFilm2, testFilm), filmStorage.searchByTitleAndDirector("search"));
    }

    @Test
    public void testSearchByDirectorAndTitleWithDuplicate() {
        Film testFilm = Film.builder()
                .name("filmsEaRch")
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.JANUARY, 1))
                .duration(100)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(director))
                .build();

        testFilm = filmStorage.create(testFilm);

        Director searchDirector = Director.builder()
                .name("DirectorForSSSSSEaRCH")
                .build();

        searchDirector = directorStorage.add(searchDirector).orElseThrow();
        Film testFilm2 = Film.builder()
                .name("filmForFindsearch")
                .description("description")
                .releaseDate(LocalDate.of(2000, Month.JANUARY, 1))
                .duration(100)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(searchDirector))
                .build();
        testFilm2 = filmStorage.create(testFilm2);

        assertEquals(List.of(testFilm2, testFilm), filmStorage.searchByTitleAndDirector("search"));
    }

    @Test
    public void testGetCommonFilms() {
        User user2 = User.builder()
                .email("email2@email.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 10))
                .build();

        userStorage.create(user2);

        User user3 = User.builder()
                .email("email3@email.ru")
                .login("login3")
                .name("name3")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 10))
                .build();

        userStorage.create(user3);

        User user4 = User.builder()
                .email("email4@email.ru")
                .login("login4")
                .name("name4")
                .birthday(LocalDate.of(2000, Month.DECEMBER, 10))
                .build();

        userStorage.create(user4);

        Film film2 = Film.builder()
                .name("film2")
                .description("description2")
                .releaseDate(LocalDate.of(2010, Month.DECEMBER, 10))
                .duration(100)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(director))
                .build();

        filmStorage.create(film2);

        Film film3 = Film.builder()
                .name("film3")
                .description("description3")
                .releaseDate(LocalDate.of(2010, Month.DECEMBER, 10))
                .duration(100)
                .mpa(new Mpa(1, null, null))
                .genres(List.of(new Genre(1, null)))
                .directors(List.of(director))
                .build();

        filmStorage.create(film3);

        likesDao.addLike(1, 1);
        likesDao.addLike(1, 2);
        likesDao.addLike(1, 3);
        likesDao.addLike(1, 4);
        likesDao.addLike(2, 1);
        likesDao.addLike(2, 2);
        likesDao.addLike(2, 3);
        likesDao.addLike(3, 1);
        likesDao.addLike(3, 2);

        assertEquals(filmStorage.getCommonFilms(1, 2), List.of(film, film2, film3));
    }
}
