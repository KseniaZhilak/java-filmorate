package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void testFindAllMpa() {
        Collection<Mpa> ratings = mpaStorage.findAll();
        assertThat(ratings).hasSize(5);
    }

    @Test
    void testFindAllMpaOrderAndNames() {
        Collection<Mpa> ratings = mpaStorage.findAll();

        assertThat(ratings)
                .extracting(Mpa::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void testFindMpaById() {
        Optional<Mpa> found = mpaStorage.findById(1L);

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(m -> {
                    assertThat(m.getId()).isEqualTo(1L);
                    assertThat(m.getName()).isEqualTo("G");
                });
    }

    @Test
    void testFindMpaByLastId() {
        Optional<Mpa> found = mpaStorage.findById(5L);

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(m -> {
                    assertThat(m.getId()).isEqualTo(5L);
                    assertThat(m.getName()).isEqualTo("NC-17");
                });
    }

    @Test
    void testFindMpaByIdNotFound() {
        Optional<Mpa> found = mpaStorage.findById(999L);
        assertThat(found).isEmpty();
    }
}