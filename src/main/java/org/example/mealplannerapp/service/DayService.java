package org.example.mealplannerapp.service;

import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.dto.DaySummaryResponse;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.AuxiliaryMapper;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.projection.stats.CategoryStats;
import org.example.mealplannerapp.projection.stats.Stats;
import org.example.mealplannerapp.projection.stats.StatsImpl;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DayService {
    
    private final DayRepository dayRepository;
    private final EntryRepository entryRepository;
    private final EntryMapper entryMapper;
    private final AuxiliaryMapper auxiliaryMapper;

    // TODO: Javadocs for service and methods.

    @Transactional
    public void deleteAllEntries(User user, Long dayId) {
        if (!dayRepository.existsByIdVerified(user.getId(), dayId)) {
            throw new ResourceNotFoundException("Requested day (id: " + dayId + ") not found.");
        }

        entryRepository.deleteAllByDay(dayId);
    }

    public List<EntryResponse> retrieveAllEntries(User user, Long dayId) {
        if (!dayRepository.existsByIdVerified(user.getId(), dayId)) {
            throw new ResourceNotFoundException("Requested day (id: " + dayId + ") not found.");
        }

        return entryRepository.fetchAllByDay(dayId)
                .stream()
                .map(entryMapper::generateResponse)
                .toList();
    }

    public DaySummaryResponse retrieveDaySummary(User user, Long dayId) {
        Day day = dayRepository.fetchByIdVerified(user.getId(), dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested day (id: " + dayId + ") not found."));

        List<CategoryStats> categoryStats = entryRepository.extractCategoryStatsByDay(dayId);

        StatsImpl dayStats = new StatsImpl(
                categoryStats.stream().mapToDouble(CategoryStats::getCalories).sum(),
                categoryStats.stream().mapToDouble(CategoryStats::getProtein).sum(),
                categoryStats.stream().mapToDouble(CategoryStats::getCarbs).sum(),
                categoryStats.stream().mapToDouble(CategoryStats::getFat).sum(),
                categoryStats.stream().mapToDouble(CategoryStats::getFiber).sum(),
                categoryStats.stream().mapToDouble(CategoryStats::getPrice).sum());

        return new DaySummaryResponse(
                categoryStats.stream().collect(Collectors.toMap(CategoryStats::getCategory, auxiliaryMapper::toStatsResponse)),
                auxiliaryMapper.toStatsResponse(dayStats),
                auxiliaryMapper.toGoalsResponse(day));
    }
}
