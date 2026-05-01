package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.Optional;

@Repository
public class GenreDbStorage implements GenreStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Genre> genreMapper = (rs, rowNum) -> {
        Genre genre = new Genre();
        genre.setId(rs.getLong("id"));
        genre.setName(rs.getString("name"));
        return genre;
    };

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Genre> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, genreMapper);
    }

    @Override
    public Optional<Genre> findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID_QUERY, genreMapper, id)
                .stream()
                .findFirst();
    }
}
