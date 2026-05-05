package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    void testFindAllGenres() {
        Collection<Genre> genres = genreStorage.findAll();
        assertThat(genres).hasSize(6);
    }

    @Test
    void testFindGenreById() {
        Optional<Genre> found = genreStorage.findById(1L);

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(g -> {
                    assertThat(g.getId()).isEqualTo(1L);
                    assertThat(g.getName()).isEqualTo("Комедия");
                });
    }

    @Test
    void testFindAllGenresOrderAndNames() {
        Collection<Genre> genres = genreStorage.findAll();

        assertThat(genres)
                .extracting(Genre::getName)
                .contains("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    void testFindGenreByIdNotFound() {
        Optional<Genre> found = genreStorage.findById(999L);
        assertThat(found).isEmpty();
    }
}