package org.example.mealplannerapp.dto.day.response;

import java.util.List;

/**
 * Response DTO for displaying daily goals, daily stats, and category stats.
 */
public record DaySummaryResponse(
        List<CategoryStatsResponse> categoryStats,
        DayStatsResponse dayStats,
        DayGoalsResponse dayGoals
) {
}
