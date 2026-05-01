package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;

@Service
public class GenreService {

    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Collection<GenreDto> getAllGenres() {
        return genreStorage.findAll()
                .stream()
                .map(GenreMapper::mapToGenreDto)
                .toList();
    }

    public GenreDto getById(Long id) {
        return genreStorage.findById(id)
                .map(GenreMapper::mapToGenreDto)
                .orElseThrow(() -> new NotFoundException("Жанр не найден с ID: " + id));
    }

}
