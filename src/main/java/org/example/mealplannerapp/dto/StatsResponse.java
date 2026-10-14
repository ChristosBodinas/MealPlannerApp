package org.example.mealplannerapp.dto;

import java.math.BigDecimal;

/**
 * General use response DTO for displaying aggregate nutrition data
 * and prices.
 */
public record StatsResponse(
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) {
}