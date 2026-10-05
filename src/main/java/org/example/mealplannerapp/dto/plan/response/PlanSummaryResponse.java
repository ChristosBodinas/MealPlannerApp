package org.example.mealplannerapp.dto.plan.response;

import org.example.mealplannerapp.dto.StatsResponse;
import org.example.mealplannerapp.dto.day.response.DayStatsResponse;

import java.util.List;

public record PlanSummaryResponse(
        PlanResponse plan,
        StatsResponse planStats,
        List<DayStatsResponse> dayStats
) {
}
