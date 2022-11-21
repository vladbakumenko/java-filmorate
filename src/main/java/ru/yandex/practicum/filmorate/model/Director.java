package ru.yandex.practicum.filmorate.model;

import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Director {
    private int id;
    @NotBlank
    private String name;

    public Director() {
    }

    public Director(int id, String name) {
        this.id = id;
        this.name = name;
    }


}
