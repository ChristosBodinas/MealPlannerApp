package org.example.mealplannerapp.dto;

import java.math.BigDecimal;

public record StatsResponse(
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) {
}
