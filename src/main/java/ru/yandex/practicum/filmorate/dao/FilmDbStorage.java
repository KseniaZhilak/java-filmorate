package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.GenreService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Primary
public class FilmDbStorage implements FilmStorage {

    private static final String ADD_GENRES_TO_FILM_QUERY = "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)";

    private final JdbcTemplate jdbcTemplate;
    
    private final RowMapper<Film> filmMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setGenres(new HashSet<>());
        return film;
    };

    @Override
    public Collection<Film> findAll() {
        return jdbcTemplate.query(
                        "SELECT id, name, description, release_date, duration FROM films ORDER BY id",
                        filmMapper
                ).stream()
                .map(this::loadGenres)
                .map(this::loadLikes)
                .toList();
    }

    @Override
    public Optional<Film> findById(Long id) {
        return jdbcTemplate.query(
                        "SELECT id, name, description, release_date, duration FROM films WHERE id = ?",
                        filmMapper,
                        id
                ).stream()
                .findFirst()
                .map(this::loadGenres)
                .map(this::loadLikes);
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO films (name, description, release_date, duration) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, Date.valueOf(film.getReleaseDate()));
            statement.setInt(4, film.getDuration());
            return statement;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        addGenreToFilm(film.getId(), film.getGenres());
        return film;
    }

    @Override
    public Film update(Film film) {
        jdbcTemplate.update(
                """
                        UPDATE films
                        SET name = ?, description = ?, release_date = ?, duration = ?
                        WHERE id = ?
                        """,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getId()
        );

        return film;
    }

    @Override
    public void delete(Film film) {
        jdbcTemplate.update("DELETE FROM films WHERE id = ?", film.getId());
    }

    private Film loadLikes(Film film) {
        Set<Long> likes = film.getLikes();
        likes.addAll(jdbcTemplate.queryForList(
                "SELECT user_id FROM film_likes WHERE film_id = ? ORDER BY user_id",
                Long.class,
                film.getId()
        ));
        return film;
    }

    @Override
    public void saveLike(Film film, User user) {
        jdbcTemplate.update(
                "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)",
                film.getId(),
                user.getId());

    }

    @Override
    public void deleteLike(Film film, User user) {
        jdbcTemplate.update(
                "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", film.getId(), user.getId()
        );
    }

    private Film loadGenres(Film film) {
        Set<Genre> genres = film.getGenres();

        genres.addAll(jdbcTemplate.query(
                "SELECT id, name FROM genres WHERE id IN (" +
                        "SELECT genre_id FROM film_genres WHERE film_id = ?)",
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                },
                film.getId()
        ));

        return film;
    }

    private void addGenreToFilm(long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        genres
                .stream()
                .map(Genre::getId)
                .forEach(genreId -> jdbcTemplate.update(ADD_GENRES_TO_FILM_QUERY, filmId, genreId));
    }

}
