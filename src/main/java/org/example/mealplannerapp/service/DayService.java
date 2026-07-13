package org.example.mealplannerapp.service;

import java.util.List;

import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

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

    // DELETE("/days/{dayId}/entries")
    public void deleteAllEntries(User user, Long dayId) {
        if (!dayRepository.existsByIdVerified(user.getId(), dayId)) {
            throw new ResourceNotFoundException("The requested day (id: " + dayId + ") was not found.");
        }

        if (entryRepository.deleteAllInDay(dayId) == 0) {
            throw new ResourceNotFoundException("The requested day (id: " + dayId + ") is already empty.");
        }
    }

    // GET("/days/{dayId}/entries")
    public List<EntryResponse> retrieveAllEntries(User user, Long dayId) {
        if (!dayRepository.existsByIdVerified(user.getId(), dayId)) {
            throw new ResourceNotFoundException("The requested day (id: " + dayId + ") was not found.");
        }

        return entryRepository.findAllInDayOrdered(dayId).stream()
            .map(entryMapper::generateResponse)
            .toList();
    }

    // summarizeDay

    // retrieveDayGoals
    
    
}
