package org.example.mealplannerapp.dto.exercise.response;

import org.example.mealplannerapp.embeddable.EffortLevel;
import org.example.mealplannerapp.entity.Exercise;

import java.util.Set;

/**
 * Response DTO for displaying {@link Exercise} and associated
 * {@link EffortLevel} data.
 */
public record ExerciseResponse(
        Long id,
        String name,
        Set<LevelResponse> levels
) {
}
