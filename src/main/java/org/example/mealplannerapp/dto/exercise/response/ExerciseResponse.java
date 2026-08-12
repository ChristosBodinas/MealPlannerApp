package org.example.mealplannerapp.dto.exercise.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record ExerciseResponse(
        Long id,
        String name,
        Set<LevelResponse> levels
) {
}
