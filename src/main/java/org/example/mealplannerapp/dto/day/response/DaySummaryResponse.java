package org.example.mealplannerapp.dto.day.response;

import org.example.mealplannerapp.dto.StatsResponse;
import org.example.mealplannerapp.entity.Day;

import java.util.List;

/**
 * Response DTO for displaying a summary of a particular
 * {@link Day}'s nutrition goals, total stats, and stats per
 * category.
 */
public record DaySummaryResponse(
        DayResponse day,
        StatsResponse dayStats,
        List<CategoryStatsResponse> categoryStats
) {
}