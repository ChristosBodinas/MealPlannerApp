package org.example.mealplannerapp.dto.exercise.response;

import java.math.BigDecimal;

public record LevelResponse(
    String intensityDesc,
    BigDecimal caloriesPerMinute
) {
}
