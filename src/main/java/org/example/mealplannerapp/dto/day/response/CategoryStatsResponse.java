package org.example.mealplannerapp.dto.day.response;

import org.example.mealplannerapp.common.Category;

import java.math.BigDecimal;

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
