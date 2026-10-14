package org.example.mealplannerapp.dto.day.response;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.Day;

import java.math.BigDecimal;

/**
 * Response DTO for displaying aggregate nutrition data and prices
 * across a particular {@link Category} of a particular {@link Day}.
 */
public record CategoryStatsResponse(
        Category category,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) {
}