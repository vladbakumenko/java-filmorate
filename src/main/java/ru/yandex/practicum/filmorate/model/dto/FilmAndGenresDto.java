package ru.yandex.practicum.filmorate.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
public class FilmAndGenresDto {
    private Integer filmId;
    private Integer genreId;
    private String nameGenre;

    public FilmAndGenresDto(Integer filmId, Integer genreId, String nameGenre) {
        this.filmId = filmId;
        this.genreId = genreId;
        this.nameGenre = nameGenre;
    }
}
