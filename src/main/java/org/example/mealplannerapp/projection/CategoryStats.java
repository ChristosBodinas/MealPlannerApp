package org.example.mealplannerapp.projection;

import org.example.mealplannerapp.common.Category;

import java.math.BigDecimal;

public record CategoryStats(
        Category category,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) {
}
