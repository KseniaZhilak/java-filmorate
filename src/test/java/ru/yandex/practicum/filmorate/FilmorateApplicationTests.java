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
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmorateApplicationTests {

    private FilmController filmController;
    private UserController userController;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();

        UserService userService = new UserService(userStorage);
        FilmService filmService = null;

        filmController = new FilmController(filmService);
        userController = new UserController(userService);
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

    @Test
    void shouldAddFriend() {
        User user1 = createUser("a@mail.com");
        User user2 = createUser("b@mail.com");

        userController.addFriend(user1.getId(), user2.getId());

        List<User> friendsOfUser1 = userController.getFriends(user1.getId());

        assertEquals(1, friendsOfUser1.size());
        assertEquals(user2.getId(), friendsOfUser1.getFirst().getId());
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = createUser("a@mail.com");
        User user2 = createUser("b@mail.com");

        userController.addFriend(user1.getId(), user2.getId());
        userController.removeFriend(user1.getId(), user2.getId());

        List<User> friends = userController.getFriends(user1.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    void shouldReturnFriends() {
        User user1 = createUser("a@mail.com");
        User user2 = createUser("b@mail.com");

        userController.addFriend(user1.getId(), user2.getId());

        List<User> friends = userController.getFriends(user1.getId());

        assertEquals(1, friends.size());
    }

    @Test
    void shouldReturnCommonFriends() {
        User user1 = createUser("a@mail.com");
        User user2 = createUser("b@mail.com");
        User user3 = createUser("c@mail.com");

        userController.addFriend(user1.getId(), user3.getId());
        userController.addFriend(user2.getId(), user3.getId());

        List<User> common = userController.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, common.size());
        assertEquals(user3.getId(), common.getFirst().getId());
    }

    @Test
    void shouldAddLike() {
        Film film = createFilm();
        User user = createUser("a@mail.com");

        filmController.addLike(film.getId(), user.getId());

        Collection<Film> popular = filmController.findAll(10);

        Film result = popular.iterator().next();

        assertEquals(1, result.getLikes().size());
    }

    @Test
    void shouldRemoveLike() {
        Film film = createFilm();
        User user = createUser("a@mail.com");

        filmController.addLike(film.getId(), user.getId());
        filmController.deleteLike(film.getId(), user.getId());

        Collection<Film> popular = filmController.findAll(10);

        Film result = popular.iterator().next();

        assertEquals(0, result.getLikes().size());
    }

    @Test
    void shouldReturnMostPopularFilms() {
        Film film1 = createFilm();
        Film film2 = createFilm();

        User user1 = createUser("a@mail.com");
        User user2 = createUser("b@mail.com");

        filmController.addLike(film1.getId(), user1.getId());
        filmController.addLike(film1.getId(), user2.getId());

        filmController.addLike(film2.getId(), user1.getId());

        Collection<Film> popular = filmController.findAll(10);

        Film first = popular.iterator().next();

        assertEquals(film1.getId(), first.getId());
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

    private User createUser(String email) {
        CreateUserRequest user = new CreateUserRequest();
        user.setEmail(email);
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return userController.create(user);
    }

    private Film createFilm() {
        CreateFilmRequest film = new CreateFilmRequest();
        film.setName("Film");
        film.setDescription("Desc");
        film.setDuration(100);
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        return filmController.create(film);
    }

}
