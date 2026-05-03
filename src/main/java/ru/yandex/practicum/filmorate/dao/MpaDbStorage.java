package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
    private final RowMapper<Mpa> mpaMapper = (rs, rowNum) -> {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getLong("id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    };

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Mpa> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, mpaMapper);
    }

    @Override
    public Optional<Mpa> findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID_QUERY, mpaMapper, id)
                .stream()
                .findFirst();
    }
}