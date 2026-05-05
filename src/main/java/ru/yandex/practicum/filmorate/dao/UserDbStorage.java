package ru.yandex.practicum.filmorate.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@Primary
public class UserDbStorage implements UserStorage {

    private static final String FIND_ALL_QUERY = "SELECT id, email, login, name, birthday FROM users ORDER BY id";
    private static final String FIND_BY_ID_QUERY = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
    private static final String INSERT_USER_QUERY =
            "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE id = ?";
    private static final String LOAD_FRIENDS_QUERY =
            "SELECT friend_id FROM user_friends WHERE user_id = ? ORDER BY friend_id";

    private static final String GET_COMMON_FRIENDS_QUERY =
            "SELECT * FROM users WHERE id IN (" +
                    "SELECT friend_id FROM user_friends WHERE user_id = ? AND friend_id IN(" +
                    "SELECT friend_id FROM user_friends WHERE user_id = ?))";

    private static final String INSERT_FRIEND_QUERY =
            "INSERT INTO user_friends (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY =
            "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, RowMappers.USER_MAPPER)
                .stream()
                .map(this::loadFriends)
                .toList();
    }

    @Override
    public Optional<User> findById(Long id) {
        return jdbcTemplate.query(FIND_BY_ID_QUERY, RowMappers.USER_MAPPER, id)
                .stream()
                .findFirst()
                .map(this::loadFriends);
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    INSERT_USER_QUERY,
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
                UPDATE_USER_QUERY,
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
        jdbcTemplate.update(DELETE_USER_QUERY, user.getId());
    }

    public User loadFriends(User user) {
        Set<Long> friends = user.getFriends();
        friends.addAll(jdbcTemplate.queryForList(LOAD_FRIENDS_QUERY, Long.class, user.getId()));
        return user;
    }

    @Override
    public List<User> getCommonFriends(User user, User other) {
        return jdbcTemplate.query(GET_COMMON_FRIENDS_QUERY, RowMappers.USER_MAPPER, user.getId(), other.getId());
    }

    @Override
    public void saveFriend(User user, User friend) {
        jdbcTemplate.update(INSERT_FRIEND_QUERY, user.getId(), friend.getId());
    }

    @Override
    public void deleteFriend(User user, User friend) {
        jdbcTemplate.update(DELETE_FRIEND_QUERY, user.getId(), friend.getId());
    }

}
