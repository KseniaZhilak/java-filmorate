package ru.yandex.practicum.filmorate.dto.film;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotations.DateAfter;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Set;

@Data
public class CreateFilmRequest {

    private Long id;

    @NotNull(message = "Наименование не может быть пустым")
    @NotBlank(message = "Наименование не может быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не может быть длиннее 200 символов")
    private String description;

    @DateAfter(date = "1895-12-28")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительным числом")
    private Integer duration;

    private Set<Genre> genres;

    private Mpa mpa;

}
