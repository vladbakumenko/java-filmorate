package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class Review {
    @Positive
    private int reviewId;
    @NotBlank
    private String content;
    @JsonProperty("isPositive")
    private Boolean isPositive;
    @Positive
    private Integer filmId;
    @Positive
    private Integer userId;
    private int useful;

    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", content='" + content + '\'' +
                ", isPositive=" + isPositive +
                ", filmId=" + filmId +
                ", userId=" + userId +
                ", useful=" + useful +
                '}';
    }

}
