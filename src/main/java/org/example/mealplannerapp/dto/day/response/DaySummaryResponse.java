package org.example.mealplannerapp.dto.day.response;

import org.example.mealplannerapp.dto.StatsResponse;

import java.util.List;

/**
 * Response DTO for displaying a summary of a particular
 * {@code Day}'s nutrition goals, total stats, and stats per
 * category.
 */
public record DaySummaryResponse(
        DayResponse day,
        StatsResponse dayStats,
        List<CategoryStatsResponse> categoryStats
) {
}