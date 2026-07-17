package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;

import org.example.mealplannerapp.dto.day.DayGoalsResponse;
import org.example.mealplannerapp.dto.day.DaySumResponse;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.projection.CategorySummary;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private void verifyDayExistsAndOwned(Long userId, Long dayId) {
        if (!dayRepository.existsByIdVerified(userId, dayId)) {
            throw new ResourceNotFoundException("Requested day (id: " + dayId + ") not found.");
        }
    }

    // TO DO: duplicateAllEntries ???

    /**
     * <p>Finds and deletes all {@link Entry} entities that belong to the {@link Day} identified by
     * {@code dayId} and owned by {@code user}.
     * </p>
     * @param user the requesting user
     * @param dayId the requested day's identifiers
     * @throws ResourceNotFoundException if the requested day is not found
     */
    @Transactional
    public void deleteAllEntries(User user, Long dayId) {
        verifyDayExistsAndOwned(user.getId(), dayId);
        entryRepository.deleteAllInDay(dayId);
    }

    
    /**
     * <p>Finds and returns the complete data of all the {@link Entry} entities that belong to
     * the {@link Day} identified by {@code dayId} and owned by {@code user}, including the associated
     * {@link Food} data. Entry data is ordered by category and position.
     * </p>
     * @param user the requesting user
     * @param dayId the requested day's identifier
     * @return a list with the full data of all the entries in the requested day, ordered by category and position
     * @throws ResourceNotFoundException if the requested day is not found
     */
    public List<EntryResponse> retrieveAllEntries(User user, Long dayId) {
        verifyDayExistsAndOwned(user.getId(), dayId);
        return entryRepository.findAllInDayOrdered(dayId).stream()
            .map(entryMapper::generateResponse)
            .toList();
    }

    /**
     * For the {@link Day} identified by {@code dayId} and owned by {@code user}, calculates the
     * nutrition totals for each category in the day and for the day as a whole.
     * @param user the requesting user
     * @param dayId the requested day's identifier
     * @return  the nutrition totals for the requested day and for each individual category
     * @throws ResourceNotFoundException if the requested day is not found
     */
    public DaySumResponse retrieveDaySummary(User user, Long dayId) {
        verifyDayExistsAndOwned(user.getId(), dayId);
        List<CategorySummary> categorySums = entryRepository.summarizeByCategoryInDay(dayId);

        return DaySumResponse.from(
            categorySums.stream().mapToDouble(CategorySummary::getCalories).sum(),
            categorySums.stream().mapToDouble(CategorySummary::getProtein).sum(),
            categorySums.stream().mapToDouble(CategorySummary::getCarbs).sum(),
            categorySums.stream().mapToDouble(CategorySummary::getFat).sum(),
            categorySums.stream().mapToDouble(CategorySummary::getFiber).sum(),
            categorySums);
    }

    /**
     * <p>Finds and returns the nutrition goals for the {@link Day} entity identified by {@code dayId} and
     * owned by {@code user}.
     * </p>
     * @param user the requesting user
     * @param dayId the requested day's identifier
     * @return the nutrition goals of the requested day
     * @throws ResourceNotFoundException if the requested day is not found
     */
    public DayGoalsResponse retrieveDayGoals(User user, Long dayId) {
        Day day = dayRepository.findByIdVerified(user.getId(), dayId)
            .orElseThrow(() -> new ResourceNotFoundException("Requested day (id: " + dayId + ") not found."));

        // NOTE: Might make this into a Day method later.
        return DayGoalsResponse.from(day);
    }

}
