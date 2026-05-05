package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Primary
public class FilmDbStorage implements FilmStorage {

    private static final String FIND_ALL_QUERY =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
            "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.id ORDER BY f.id";
    private static final String FIND_BY_ID_QUERY =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
            "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";
    private static final String INSERT_FILM_QUERY =
            "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE id = ?";
    private static final String LOAD_LIKES_QUERY =
            "SELECT user_id FROM film_likes WHERE film_id = ? ORDER BY user_id";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY =
            "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
    private static final String ADD_GENRES_TO_FILM_QUERY = "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)";
    private static final String LOAD_GENRES_QUERY =
            "SELECT id, name FROM genres WHERE id IN (SELECT genre_id FROM film_genres WHERE film_id = ?)";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Film> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, RowMappers.FILM_MAPPER)
                .stream()
                .map(this::loadGenres)
                .map(this::loadLikes)
                .toList();
    }

    @Override
    public Optional<Film> findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID_QUERY, RowMappers.FILM_MAPPER, id)
                .stream()
                .findFirst()
                .map(this::loadGenres)
                .map(this::loadLikes);
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    INSERT_FILM_QUERY,
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setDate(3, Date.valueOf(film.getReleaseDate()));
            statement.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                statement.setLong(5, film.getMpa().getId());
            } else {
                statement.setNull(5, java.sql.Types.INTEGER);
            }
            return statement;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        addGenreToFilm(film.getId(), film.getGenres());
        return film;
    }

    @Override
    public Film update(Film film) {
        jdbcTemplate.update(
                UPDATE_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId()
        );
        return film;
    }

    @Override
    public void delete(Film film) {
        jdbcTemplate.update(DELETE_FILM_QUERY, film.getId());
    }

    private Film loadLikes(Film film) {
        Set<Long> likes = film.getLikes();
        likes.addAll(jdbcTemplate.queryForList(LOAD_LIKES_QUERY, Long.class, film.getId()));
        return film;
    }

    @Override
    public void saveLike(Film film, User user) {
        jdbcTemplate.update(INSERT_LIKE_QUERY, film.getId(), user.getId());
    }

    @Override
    public void deleteLike(Film film, User user) {
        jdbcTemplate.update(DELETE_LIKE_QUERY, film.getId(), user.getId());
    }

    private Film loadGenres(Film film) {
        Set<Genre> genres = film.getGenres();
        genres.addAll(jdbcTemplate.query(LOAD_GENRES_QUERY, RowMappers.GENRE_MAPPER, film.getId()));
        return film;
    }

    private void addGenreToFilm(long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        genres.stream()
                .map(Genre::getId)
                .forEach(genreId -> jdbcTemplate.update(ADD_GENRES_TO_FILM_QUERY, filmId, genreId));
    }
}