package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaStorage mpaStorage;

    public Collection<MpaDto> getAllMpa() {
        return mpaStorage.findAll()
                .stream()
                .map(MpaMapper::mapToMpaDto)
                .toList();
    }

    public MpaDto getById(Long id) {
        return mpaStorage.findById(id)
                .map(MpaMapper::mapToMpaDto)
                .orElseThrow(() -> new NotFoundException("Рейтинг не найден с ID: " + id));
    }
}