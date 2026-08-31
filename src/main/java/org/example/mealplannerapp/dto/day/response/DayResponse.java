package org.example.mealplannerapp.dto.day.response;

import org.example.mealplannerapp.entity.Day;

import java.math.BigDecimal;

/**
 * Response DTO for displaying {@link Day} data.
 */
public record DayResponse(
        Long id,
        int position,
        BigDecimal targetCalories,
        BigDecimal targetProtein,
        BigDecimal targetCarbs,
        BigDecimal targetFat,
        BigDecimal targetFiber
) {
}
