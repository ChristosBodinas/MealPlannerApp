package org.example.mealplannerapp.projection;

import java.math.BigDecimal;

public record Goals(
        BigDecimal targetCalories,
        BigDecimal targetProtein,
        BigDecimal targetCarbs,
        BigDecimal targetFat,
        BigDecimal targetFiber
) {
}
