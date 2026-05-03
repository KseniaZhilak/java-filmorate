package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.film.CreateFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable("id") long filmId) {
        return filmService.getById(filmId);
    }

    @PostMapping
    public Film create(@Valid @RequestBody CreateFilmRequest request) {
        Film film = new Film();

        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setDuration(request.getDuration());
        film.setReleaseDate(request.getReleaseDate());
        film.setGenres(request.getGenres());
        film.setMpa(request.getMpa());

        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody UpdateFilmRequest newFilm) {
        Film film = filmService.getById(newFilm.getId());

        film.setName(newFilm.getName());

        if (newFilm.getDescription() != null) {
            film.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            film.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null) {
            film.setDuration(newFilm.getDuration());
        }
        if (newFilm.getMpa() != null) {
            film.setMpa(newFilm.getMpa());
        }

        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") long filmId,
                        @PathVariable long userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") long filmId,
                           @PathVariable long userId) {
        filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> findAll(
            @RequestParam(defaultValue = "10") int count
    ) {
        return filmService.findByCount(count);
    }

}
