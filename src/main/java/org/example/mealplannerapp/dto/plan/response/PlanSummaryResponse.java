package org.example.mealplannerapp.dto.plan.response;

import org.example.mealplannerapp.dto.StatsResponse;
import org.example.mealplannerapp.dto.day.response.DayStatsResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;

import java.util.List;

/**
 * Response DTO for displaying a summary of a particular
 * {@link Plan}'s nutrition goals, total stats, and stats per
 * {@link Day}.
 */
public record PlanSummaryResponse(
        PlanResponse plan,
        StatsResponse planStats,
        List<DayStatsResponse> dayStats
) {
}