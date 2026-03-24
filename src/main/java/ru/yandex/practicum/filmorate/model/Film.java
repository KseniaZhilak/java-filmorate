package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.Instant;

@Data
public class Film {

    private Long id;
    private String name;
    private String description;
    private Instant releaseDate;
    private Integer duration;

}
