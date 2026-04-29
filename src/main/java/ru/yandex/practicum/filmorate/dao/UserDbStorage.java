package ru.yandex.practicum.filmorate.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
@Primary
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    };

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> findAll() {
        return jdbcTemplate.query(
                        "SELECT id, email, login, name, birthday FROM users ORDER BY id",
                        userMapper
                ).stream()
                .map(this::loadFriends)
                .toList();
    }

    @Override
    public Optional<User> findById(Long id) {
        return jdbcTemplate.query(
                        "SELECT id, email, login, name, birthday FROM users WHERE id = ?",
                        userMapper,
                        id
                ).stream()
                .findFirst()
                .map(this::loadFriends);
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());
            statement.setDate(4, Date.valueOf(user.getBirthday()));
            return statement;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(
                """
                        UPDATE users
                        SET email = ?, login = ?, name = ?, birthday = ?
                        WHERE id = ?
                        """,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
        return user;
    }

    @Override
    public void delete(User user) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", user.getId());
    }

    public User loadFriends(User user) {
        Set<Long> friends = user.getFriends();
        friends.addAll(jdbcTemplate.queryForList(
                "SELECT friend_id FROM user_friends WHERE user_id = ? ORDER BY friend_id",
                Long.class,
                user.getId()
        ));
        return user;
    }

    @Override
    public void saveFriend(User user, User friend) {
        jdbcTemplate.update(
                "INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)",
                user.getId(),
                friend.getId());
    }

    @Override
    public void deleteFriend(User user, User friend) {
        jdbcTemplate.update(
                "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?", user.getId(), friend.getId()
        );
    }

}
