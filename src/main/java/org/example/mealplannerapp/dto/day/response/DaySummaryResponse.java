package org.example.mealplannerapp.dto.day.response;

import org.example.mealplannerapp.dto.StatsResponse;

import java.util.List;

public record DaySummaryResponse(
        DayResponse day,
        StatsResponse dayStats,
        List<CategoryStatsResponse> categoryStats
) {
}
