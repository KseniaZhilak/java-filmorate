package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.user.CreateUserRequest;
import ru.yandex.practicum.filmorate.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable("id") long userId) {
        return userService.getById(userId);
    }

    @PostMapping
    public User create(@Valid @RequestBody CreateUserRequest request) {

        User user = new User();

        user.setEmail(request.getEmail());
        user.setLogin(request.getLogin());
        user.setName(request.getName());
        user.setBirthday(request.getBirthday());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody UpdateUserRequest newUser) {
        User user = userService.getById(newUser.getId());
        if (newUser.getEmail() != null) {
            if (newUser.getEmail().equals(user.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
            user.setEmail(newUser.getEmail());
        }

        if (newUser.getName() != null) {
            user.setName(newUser.getName());
        }

        if (newUser.getLogin() != null) {
            user.setLogin(newUser.getLogin());
        }

        if (newUser.getBirthday() != null) {
            user.setBirthday(newUser.getBirthday());
        }
        return userService.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id,
                          @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id,
                             @PathVariable Long friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id,
                                       @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

}
