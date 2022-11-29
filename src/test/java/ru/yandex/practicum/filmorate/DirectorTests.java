package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.storage.dao.DirectorDao;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DirectorTests {

    private final DirectorDao directorDao;
    private final DirectorService directorService;

    @Test
    @Order(1)
    public void testCreateDirector() {
        Director director = Director.builder().name("Test director").build();
        var createdDirector = directorDao.add(director).orElseThrow();

        assertThat(createdDirector)
                .hasFieldOrPropertyWithValue("id", 1);
    }

    @Test
    @Order(2)
    public void testGetDirector() {
        Director director = directorDao.findById(1).orElseThrow();

        assertThat(director)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Test director");
    }

    @Test
    @Order(3)
    public void testUpdateDirector() {
        Director directorForUpdate = Director.builder().id(1).name("Updated director").build();
        Director updatedDirector = directorDao.update(directorForUpdate).orElseThrow();

        assertThat(updatedDirector)
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Updated director");
    }

    @Test
    @Order(4)
    public void testFindAllDirectors() {
        var directors = directorDao.findAll();

        assertThat(directors)
                .hasSize(1)
                .containsExactly(directorDao.findById(1).orElseThrow());
    }

    @Test
    @Order(5)
    public void testDeleteDirector() {
        directorDao.delete(1);

        Exception exception = assertThrows(RuntimeException.class,
                () -> directorService.getById(1));

        assertThat(exception.getMessage())
                .contains("Director with id: 1 not found in DB");
    }
}
