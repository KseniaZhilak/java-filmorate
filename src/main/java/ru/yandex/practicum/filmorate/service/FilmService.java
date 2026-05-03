package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final GenreService genreService;
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final MpaService mpaService;

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        verifyMpa(film);
        verifyGenres(film);
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

        filmStorage.saveLike(film, user);

    }

    public void deleteLike(long filmId, long userId) {
        Film film = getById(filmId);
        User user = userService.getById(userId);

        filmStorage.deleteLike(film, user);
    }

    public Collection<Film> findByCount(int count) {
        return findAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        f2.getLikes().size(), f1.getLikes().size()
                ))
                .limit(count).toList();
    }

    private void verifyGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }
        film.getGenres().forEach(e -> genreService.getById(e.getId()));
    }

    private void verifyMpa(Film film) {
        if (film.getMpa() != null) {
            mpaService.getById(film.getMpa().getId());
        }
    }


}
