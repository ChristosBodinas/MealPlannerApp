package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.day.response.DaySummaryResponse;
import org.example.mealplannerapp.dto.entry.response.listed.ListedEntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.DayMapper;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.projection.CategoryStats;
import org.example.mealplannerapp.projection.Stats;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A service responsible for read, delete, and summarize operations
 * on multiple {@link Entry} entities across the same {@link Day}.
 */
@Service
@AllArgsConstructor
public class DayService {

    private final DayRepository dayRepository;
    private final EntryRepository entryRepository;
    private final DayMapper dayMapper;
    private final EntryMapper entryMapper;

    /**
     * Deletes all {@link Entry} entities in the {@link Day} identified by
     * {@code dayId} and owned by {@code user}.
     *
     * @param user  the user making the request
     * @param dayId the identifier of the day whose entries are to be deleted
     * @throws ResourceNotFoundException if {@code dayId} does not correspond to
     *                                   an existing day owned by {@code user}
     */
    @Transactional
    public void deleteAllEntries(
            User user, Long dayId
    ) {
        Long userId = user.getId();

        if (!dayRepository.existsByIdVerified(userId, dayId)) {
            throw new ResourceNotFoundException(
                    "Requested day (id: " + dayId + ") not found.");
        }

        entryRepository.deleteByDay(dayId);
    }

    /**
     * Retrieves all {@link Entry} entities in the {@link Day} identified by
     * {@code dayId} and owned by {@code user}.
     *
     * <p>Does not retrieve the data of any referenced {@link Food} or
     * {@link Exercise} entities.</p>
     *
     * @param user  the user making the request
     * @param dayId the identifier of the day whose entries are to be retrieved
     * @return a list of all entries' data in the given day, without the data
     * of the referenced foods and exercises
     * @throws ResourceNotFoundException if {@code dayId} does not correspond to
     *                                   an existing day owned by {@code user}
     */
    @Transactional(readOnly = true)
    public List<ListedEntryResponse> retrieveAllEntries(
            User user, Long dayId
    ) {
        Long userId = user.getId();

        if (!dayRepository.existsByIdVerified(userId, dayId)) {
            throw new ResourceNotFoundException(
                    "Requested day (id: " + dayId + ") not found.");
        }

        return entryRepository.fetchShallowByDayOrdered(dayId)
                .stream()
                .map(entryMapper::toListedResponse)
                .toList();
    }

    /**
     * Produces a summary of nutrition goals, per-category nutrition/price stats,
     * and total nutrition/price stats for the {@link Day} identified by {@code dayId}
     * and owned by {@code user}.
     *
     * @param user  the user making the request
     * @param dayId the identifier of the day to be summarized
     * @return a summary of the requested day's goals, stats, and per-category stats
     * @throws ResourceNotFoundException if {@code dayId} does not correspond to an
     *                                   existing day owned by {@code user}
     */
    @Transactional(readOnly = true)
    public DaySummaryResponse summarizeDay(
            User user, Long dayId
    ) {
        Long userId = user.getId();

        Day day = dayRepository.fetchByIdVerified(userId, dayId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested day (id: " + dayId + ") not found."));

        List<CategoryStats> categoryStats = entryRepository.summarizeCategoriesByDay(dayId);

        Stats dayStats = new Stats(
                categoryStats.stream().map(CategoryStats::calories).reduce(BigDecimal.ZERO, BigDecimal::add),
                categoryStats.stream().map(CategoryStats::protein).reduce(BigDecimal.ZERO, BigDecimal::add),
                categoryStats.stream().map(CategoryStats::carbs).reduce(BigDecimal.ZERO, BigDecimal::add),
                categoryStats.stream().map(CategoryStats::fat).reduce(BigDecimal.ZERO, BigDecimal::add),
                categoryStats.stream().map(CategoryStats::fiber).reduce(BigDecimal.ZERO, BigDecimal::add),
                categoryStats.stream().map(CategoryStats::price).reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        // Fill out category stats with "empty" categories.
        List<CategoryStats> completeCategoryStats;
        if (categoryStats.size() < Category.values().length) {
            completeCategoryStats = new ArrayList<>(Category.values().length);

            Map<Category, CategoryStats> mappedCategoryStats = categoryStats.stream().collect(Collectors.toMap(
                    CategoryStats::category, Function.identity()));

            for (Category category : Category.values()) {
                if (mappedCategoryStats.containsKey(category)) {
                    completeCategoryStats.add(mappedCategoryStats.get(category));
                } else {
                    completeCategoryStats.add(new CategoryStats(category,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
                }
            }
        } else {
            completeCategoryStats = categoryStats;
        }

        return dayMapper.toSummaryResponse(day, dayStats, completeCategoryStats);
    }
}