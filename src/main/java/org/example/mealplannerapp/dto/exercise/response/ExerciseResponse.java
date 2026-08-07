package org.example.mealplannerapp.dto.exercise.response;

import java.util.Set;
import lombok.Builder;

@Builder
public record ExerciseResponse(
    Long id,
    String name,
    Set<LevelResponse> levels
) {
}
