package org.example.mealplannerapp.service;

import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DayService {
    
    private final DayRepository dayRepository;
    private final EntryRepository entryRepository;

    @Transactional
    public void deleteAllEntries(User user, Long dayId) {
        if (!dayRepository.existsByIdVerified(user.getId(), dayId)) {
            throw new ResourceNotFoundException("Requested day (id: " + dayId + ") not found.");
        }

        entryRepository.deleteAllByDay(dayId);
    }

    // retrieveAllEntries
    // TODO: polymorphic fetchAllByDayVerified repository method?

    // retrieveSummaries
}
