package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film getById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public void addLike(long filmId, long userId) {
        Film film = getById(filmId);
        User user = userService.getById(userId);
        film.getLikes().add(user.getId());
        filmStorage.update(film);
    }

    public void deleteLike(long filmId, long userId) {
        Film film = getById(filmId);
        User user = userService.getById(userId);
        film.getLikes().remove(user.getId());
        filmStorage.update(film);
    }

    public Collection<Film> findByCount(int count) {
        return findAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        f2.getLikes().size(), f1.getLikes().size()
                ))
                .limit(count).toList();
    }

}
