package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.day.response.DaySummaryResponse;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.DayMapper;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.projection.CategoryStats;
import org.example.mealplannerapp.projection.impl.CategoryStatsImpl;
import org.example.mealplannerapp.projection.impl.GoalsImpl;
import org.example.mealplannerapp.projection.impl.StatsImpl;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DayService {

    private final EntryRepository entryRepository;
    private final DayRepository dayRepository;
    private final EntryMapper entryMapper;
    private final DayMapper dayMapper;

    private void fillMissingCategoryStats(List<CategoryStats> categoryStats) {
        Map<Category, CategoryStats> categoryMap = categoryStats
                .stream()
                .collect(Collectors.toMap(CategoryStats::getCategory, Function.identity()));

        for (Category category : Category.values()) {
            categoryMap.putIfAbsent(category, new CategoryStatsImpl(
                    category, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        }

        categoryStats.clear();
        for (Category category : Category.values()) {
            categoryStats.add(categoryMap.get(category));
        }
    }

    /**
     * Deletes all {@link Entry} entities that belong to the {@link Day} identified by {@code dayId}
     * and owned by {@code user}.
     *
     * @param user  the user making the request
     * @param dayId the identifier of the day whose entries are to be deleted
     * @throws ResourceNotFoundException when the given day does not exist or belongs to another user
     */
    public void deleteAllEntries(User user, Long dayId) {
        if (!dayRepository.existsByIdVerified(user.getId(), dayId)) {
            throw new ResourceNotFoundException("Requested day (id: " + dayId + ") not found.");
        }

        entryRepository.deleteByDay(dayId);
    }

    /**
     * Retrieves all {@link Entry} entities that belong to the {@link Day} identified by {@code dayId}
     * and owned by {@code user}. Also returns the full data of any associated {@link Food} or
     * {@link Exercise} entities.
     *
     * @param user  the user making the request
     * @param dayId the identifier of the day whose entries to retrieve
     * @return a list with the full data of the given day's entries and their referenced foods/exercises
     * @throws ResourceNotFoundException when the given day does not exist or belongs to another user
     */
    public List<EntryResponse> retrieveAllEntries(User user, Long dayId) {
        if (!dayRepository.existsByIdVerified(user.getId(), dayId)) {
            throw new ResourceNotFoundException("Requested day (id: " + dayId + ") not found.");
        }

        return entryRepository.fetchByDayOrdered(dayId)
                .stream()
                .map(entryMapper::toResponse)
                .toList();
    }

    /**
     * Calculates the nutrition and price totals for each category in the {@link Day} identified by {@code dayId}
     * as well as the day as a whole. Also, retrieves the nutritional goals for the given day.
     *
     * @param user  the user making the request
     * @param dayId the identifier for the day to summarize
     * @return the day's nutrition goals, its total nutrition/price values, and the nutrition/price totals per category
     * @throws ResourceNotFoundException when the given day does not exist or belongs to another user
     */
    public DaySummaryResponse summarizeDay(User user, Long dayId) {
        Day day = dayRepository.fetchByIdVerified(user.getId(), dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested day (id: " + dayId + ") not found."));

        List<CategoryStats> categoryStats = entryRepository.sumSnapshotsByDayGroupedByCategory(dayId);

        StatsImpl dayStats = new StatsImpl(
                categoryStats.stream().mapToDouble(CategoryStats::getCalories).sum(),
                categoryStats.stream().mapToDouble(CategoryStats::getProtein).sum(),
                categoryStats.stream().mapToDouble(CategoryStats::getCarbs).sum(),
                categoryStats.stream().mapToDouble(CategoryStats::getFat).sum(),
                categoryStats.stream().mapToDouble(CategoryStats::getFiber).sum(),
                categoryStats.stream().mapToDouble(CategoryStats::getPrice).sum()
        );

        fillMissingCategoryStats(categoryStats);

        GoalsImpl dayGoals = day.retrieveGoals();

        return dayMapper.toSummaryResponse(categoryStats, dayStats, dayGoals);
    }

    // TODO: Modify these methods to find Days by planId and day Position?

}
