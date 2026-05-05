package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserStorage {

    Collection<User> findAll();

    Optional<User> findById(Long id);

    User create(User user);

    User update(User user);

    void delete(User user);

    List<User> getCommonFriends(User user, User other);

    void saveFriend(User user, User friend);

    void deleteFriend(User user, User friend);


}
