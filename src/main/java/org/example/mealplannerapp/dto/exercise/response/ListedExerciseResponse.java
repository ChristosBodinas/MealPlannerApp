package org.example.mealplannerapp.dto.exercise.response;

import lombok.Builder;

@Builder
public record ListedExerciseResponse(
    Long id,
    String name
) {
}