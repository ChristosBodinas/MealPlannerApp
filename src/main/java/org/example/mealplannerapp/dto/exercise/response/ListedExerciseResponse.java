package org.example.mealplannerapp.dto.exercise.response;

import org.example.mealplannerapp.embeddable.EffortLevel;
import org.example.mealplannerapp.entity.Exercise;

/**
 * Response DTO for displaying {@link Exercise} data without the
 * associated {@link EffortLevel} data. Intended for listing multiple exercises.
 */
public record ListedExerciseResponse(
        Long id,
        String name
) {
}
