package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.film.CreateFilmRequest;
import ru.yandex.practicum.filmorate.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dto.user.CreateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTests {

    private FilmController filmController;
    private UserController userController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        userController = new UserController(null);
    }

    @Test
    void shouldCreateFilm() {
        Film film = filmController.create(buildFilm());

        assertNotNull(film.getId());
        assertEquals("Film", film.getName());
    }

    @Test
    void shouldReturnAllFilms() {
        filmController.create(buildFilm());

        Collection<Film> films = filmController.findAll();

        assertEquals(1, films.size());
    }

    @Test
    void shouldUpdateFilm() {
        Film created = filmController.create(buildFilm());

        UpdateFilmRequest update = new UpdateFilmRequest();
        update.setId(created.getId());
        update.setName("Updated");

        Film updated = filmController.update(update);

        assertEquals("Updated", updated.getName());
    }

    @Test
    void shouldThrowWhenFilmNotFound() {
        UpdateFilmRequest update = new UpdateFilmRequest();
        update.setId(999L);

        assertThrows(NotFoundException.class,
                () -> filmController.update(update));
    }

    @Test
    void shouldCreateUser() {
        User user = userController.create(buildUser());

        assertNotNull(user.getId());
        assertEquals("test@mail.com", user.getEmail());
    }

    @Test
    void shouldUseLoginAsNameIfBlank() {
        CreateUserRequest request = buildUser();
        request.setName("");

        User user = userController.create(request);

        assertEquals("login", user.getName());
    }

    @Test
    void shouldReturnAllUsers() {
        userController.create(buildUser());

        Collection<User> users = userController.findAll();

        assertEquals(1, users.size());
    }

    @Test
    void shouldUpdateUser() {
        User created = userController.create(buildUser());

        UpdateUserRequest update = new UpdateUserRequest();
        update.setId(created.getId());
        update.setEmail("new@mail.com");

        User updated = userController.update(update);

        assertEquals("new@mail.com", updated.getEmail());
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        UpdateUserRequest update = new UpdateUserRequest();
        update.setId(999L);

        assertThrows(NotFoundException.class,
                () -> userController.update(update));
    }

    @Test
    void shouldThrowWhenDuplicateEmail() {
        User created = userController.create(buildUser());

        UpdateUserRequest update = new UpdateUserRequest();
        update.setId(created.getId());
        update.setEmail(created.getEmail());

        assertThrows(DuplicatedDataException.class,
                () -> userController.update(update));
    }

    private CreateFilmRequest buildFilm() {
        CreateFilmRequest request = new CreateFilmRequest();
        request.setName("Film");
        request.setDescription("Desc");
        request.setDuration(100);
        request.setReleaseDate(LocalDate.of(2020, 1, 1));
        return request;
    }

    private CreateUserRequest buildUser() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@mail.com");
        request.setLogin("login");
        request.setName("name");
        request.setBirthday(LocalDate.of(2000, 1, 1));
        return request;
    }
}
