package org.example.mealplannerapp.dto.day.response;

import java.math.BigDecimal;

public record DayStatsResponse(
        Long dayId,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) {
}
