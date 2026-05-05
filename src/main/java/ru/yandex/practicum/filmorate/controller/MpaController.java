package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<MpaDto> getAllMpa() {
        return mpaService.getAllMpa();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MpaDto getMpaById(@PathVariable("id") Long id) {
        return mpaService.getById(id);
    }
}