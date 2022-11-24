package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;

@Data
@Builder
public class Review {
    @Min(0)
    private int reviewId;
    @NotBlank
    private String content;
    @JsonProperty("isPositive")
    @NotNull
    private Boolean isPositive;
    private Integer filmId;
    private Integer userId;
    private int useful;

}
