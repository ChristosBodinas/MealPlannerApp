package org.example.mealplannerapp.projection;

import java.math.BigDecimal;

public record Stats(
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) {
}
