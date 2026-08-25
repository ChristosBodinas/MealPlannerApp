package org.example.mealplannerapp.dto.exercise.response;

import org.example.mealplannerapp.embeddable.EffortLevel;

import java.math.BigDecimal;

/**
 * Response DTO for displaying {@link EffortLevel} data.
 */
public record LevelResponse(
        String name,
        BigDecimal burnRate
) {
}
