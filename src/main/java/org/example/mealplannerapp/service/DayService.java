package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>A service that handles operations on individual {@link Day} entities, as well as
 * bulk operations on {@link Entry} entities across an entire {@link Day}.
 * </p>
 */
@Service
@AllArgsConstructor
public class DayService {

    private final DayRepository dayRepository;
    private final EntryRepository entryRepository;
    private final EntryMapper entryMapper;

    // duplicateAllEntries

    // deleteAllEntries

    // retrieveAllEntries

    // summarizeDay (includes category and day sum)

    // retrieveDayGoals


}
