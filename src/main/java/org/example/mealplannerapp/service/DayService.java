package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.day.response.DaySummaryResponse;
import org.example.mealplannerapp.dto.entry.response.listed.ListedEntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.User;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@AllArgsConstructor
public class DayService {

    private final DayRepository dayRepository;
    private final EntryRepository entryRepository;
    private final DayMapper dayMapper;
    private final EntryMapper entryMapper;

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
        List<CategoryStats> completeCategoryStats = new ArrayList<>(Category.values().length);

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

        return dayMapper.toSummaryResponse(day, dayStats, completeCategoryStats);
    }
}
