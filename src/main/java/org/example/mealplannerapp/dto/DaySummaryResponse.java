package org.example.mealplannerapp.dto;

import org.example.mealplannerapp.constants.Category;

import java.util.Map;

/**
 * Response DTO for displaying the daily goals, daily totals, and category totals of a given day.
 */
public record DaySummaryResponse(
        Map<Category, StatsResponse> categoryStats,
        StatsResponse dayStats,
        GoalsResponse dayGoals
) {
}
