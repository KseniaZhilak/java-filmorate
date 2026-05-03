package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    private User buildUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void testCreateUser() {
        User created = userStorage.create(buildUser("create@test.com", "createlogin"));
        assertThat(created.getId()).isNotNull().isPositive();
    }

    @Test
    void testFindUserById() {
        User created = userStorage.create(buildUser("find@test.com", "findlogin"));

        Optional<User> found = userStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(created.getId());
                    assertThat(u.getEmail()).isEqualTo("find@test.com");
                    assertThat(u.getLogin()).isEqualTo("findlogin");
                    assertThat(u.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    void testFindUserByIdNotFound() {
        Optional<User> found = userStorage.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void testFindAllUsers() {
        userStorage.create(buildUser("all1@test.com", "login1"));
        userStorage.create(buildUser("all2@test.com", "login2"));

        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void testUpdateUser() {
        User created = userStorage.create(buildUser("upd@test.com", "updlogin"));
        created.setName("Updated Name");
        created.setEmail("updated@test.com");
        userStorage.update(created);

        Optional<User> found = userStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getName()).isEqualTo("Updated Name");
                    assertThat(u.getEmail()).isEqualTo("updated@test.com");
                });
    }

    @Test
    void testDeleteUser() {
        User created = userStorage.create(buildUser("del@test.com", "dellogin"));
        userStorage.delete(created);

        Optional<User> found = userStorage.findById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void testSaveFriend() {
        User user = userStorage.create(buildUser("user@test.com", "userlogin"));
        User friend = userStorage.create(buildUser("friend@test.com", "friendlogin"));
        userStorage.saveFriend(user, friend);

        Optional<User> found = userStorage.findById(user.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(u -> assertThat(u.getFriends()).containsExactly(friend.getId()));
    }

    @Test
    void testDeleteFriend() {
        User user = userStorage.create(buildUser("user2@test.com", "userlogin2"));
        User friend = userStorage.create(buildUser("friend2@test.com", "friendlogin2"));
        userStorage.saveFriend(user, friend);
        userStorage.deleteFriend(user, friend);

        Optional<User> found = userStorage.findById(user.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(u -> assertThat(u.getFriends()).isEmpty());
    }
}