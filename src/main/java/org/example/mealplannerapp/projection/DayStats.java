package org.example.mealplannerapp.projection;

import java.math.BigDecimal;

public record DayStats(
        Long id,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) {
}
