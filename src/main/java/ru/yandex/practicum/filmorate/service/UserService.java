package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User getById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        User user = getById(userId);
        User friend = getById(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getById(userId);
        User friend = getById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = getById(userId);
        User other = getById(otherId);

        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(this::getById)
                .toList();
    }

    public List<User> getFriends(Long userId) {
        User user = getById(userId);

        return user.getFriends().stream()
                .map(this::getById)
                .toList();
    }

}
