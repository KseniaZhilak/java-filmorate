package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film buildFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Test description");
        film.setReleaseDate(LocalDate.of(2000, 6, 15));
        film.setDuration(120);
        Mpa mpa = new Mpa();
        mpa.setId(1L);
        film.setMpa(mpa);
        return film;
    }

    private User buildUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void testCreateFilm() {
        Film created = filmStorage.create(buildFilm("Create Test"));

        assertThat(created.getId()).isNotNull().isPositive();
        assertThat(created.getName()).isEqualTo("Create Test");
    }

    @Test
    void testFindFilmById() {
        Film created = filmStorage.create(buildFilm("Find Test"));

        Optional<Film> found = filmStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getId()).isEqualTo(created.getId());
                    assertThat(f.getName()).isEqualTo("Find Test");
                    assertThat(f.getDuration()).isEqualTo(120);
                    assertThat(f.getReleaseDate()).isEqualTo(LocalDate.of(2000, 6, 15));
                });
    }

    @Test
    void testFindFilmByIdNotFound() {
        Optional<Film> found = filmStorage.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void testFindAllFilms() {
        filmStorage.create(buildFilm("Film One"));
        filmStorage.create(buildFilm("Film Two"));

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    void testCreateFilmWithMpa() {
        Film film = buildFilm("MPA Test");
        film.getMpa().setId(3L);
        Film created = filmStorage.create(film);

        Optional<Film> found = filmStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getMpa()).isNotNull();
                    assertThat(f.getMpa().getId()).isEqualTo(3L);
                    assertThat(f.getMpa().getName()).isEqualTo("PG-13");
                });
    }

    @Test
    void testUpdateFilm() {
        Film created = filmStorage.create(buildFilm("Original Name"));
        created.setName("Updated Name");
        created.setDuration(90);
        Mpa newMpa = new Mpa();
        newMpa.setId(2L);
        created.setMpa(newMpa);
        filmStorage.update(created);

        Optional<Film> found = filmStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getName()).isEqualTo("Updated Name");
                    assertThat(f.getDuration()).isEqualTo(90);
                    assertThat(f.getMpa().getId()).isEqualTo(2L);
                    assertThat(f.getMpa().getName()).isEqualTo("PG");
                });
    }

    @Test
    void testDeleteFilm() {
        Film created = filmStorage.create(buildFilm("Delete Test"));
        filmStorage.delete(created);

        Optional<Film> found = filmStorage.findById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void testSaveLike() {
        Film film = filmStorage.create(buildFilm("Like Test"));
        User user = userStorage.create(buildUser("like@test.com", "likelogin"));
        filmStorage.saveLike(film, user);

        Optional<Film> found = filmStorage.findById(film.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getLikes()).containsExactly(user.getId()));
    }

    @Test
    void testDeleteLike() {
        Film film = filmStorage.create(buildFilm("Unlike Test"));
        User user = userStorage.create(buildUser("unlike@test.com", "unlikelogin"));
        filmStorage.saveLike(film, user);
        filmStorage.deleteLike(film, user);

        Optional<Film> found = filmStorage.findById(film.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(f -> assertThat(f.getLikes()).isEmpty());
    }
}