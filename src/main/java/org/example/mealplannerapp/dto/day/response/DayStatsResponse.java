package org.example.mealplannerapp.dto.day.response;

import java.math.BigDecimal;

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
