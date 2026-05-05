package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
public class MpaDbStorage implements MpaStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_ratings ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_ratings WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Mpa> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, RowMappers.MPA_MAPPER);
    }

    @Override
    public Optional<Mpa> findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID_QUERY, RowMappers.MPA_MAPPER, id)
                .stream()
                .findFirst();
    }
}