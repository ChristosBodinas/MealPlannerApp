package org.example.mealplannerapp.dto.day.response;

import org.example.mealplannerapp.entity.Day;

import java.math.BigDecimal;

/**
 * Response DTO for displaying aggregate nutrition data and prices
 * for a particular {@link Day}.
 */
public record DayStatsResponse(
        Long id,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) {
}