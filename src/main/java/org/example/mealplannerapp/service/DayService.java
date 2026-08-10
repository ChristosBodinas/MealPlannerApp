package org.example.mealplannerapp.service;

import java.util.List;

import org.example.mealplannerapp.dto.day.response.DaySummaryResponse;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.DayMapper;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.projection.CategoryStats;
import org.example.mealplannerapp.projection.impl.GoalsImpl;
import org.example.mealplannerapp.projection.impl.StatsImpl;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DayService {
    
    private final EntryRepository entryRepository;
    private final DayRepository dayRepository;
    private final EntryMapper entryMapper;
    private final DayMapper dayMapper;

    // TODO: Javadocs for all.

    public void deleteAllEntries(User user, Long dayId) {
        if (!dayRepository.existsByIdVerified(user.getId(), dayId)) {
            throw new ResourceNotFoundException("Requested day (id: " + dayId + ") not found.");
        }

        entryRepository.deleteByDay(dayId);
    }

    public List<EntryResponse> retrieveAllEntries(User user, Long dayId) {
        if (!dayRepository.existsByIdVerified(user.getId(), dayId)) {
            throw new ResourceNotFoundException("Requested day (id: " + dayId + ") not found.");
        }

        return entryRepository.fetchByDayOrdered(dayId)
            .stream()
            .map(entryMapper::toResponse)
            .toList();
    }

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

        GoalsImpl dayGoals = day.retrieveGoals();

        return dayMapper.toSummaryResponse(categoryStats, dayStats, dayGoals);
        
    }

}
