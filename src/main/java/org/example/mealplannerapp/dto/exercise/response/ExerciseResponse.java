package org.example.mealplannerapp.dto.exercise.response;

import java.util.Set;

public record ExerciseResponse(
    Long id,
    String name,
    Set<IntensityResponse> intensities
) {
}
