package org.example.mealplannerapp.dto.entry.response;

import lombok.Builder;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;

/**
 * Response DTO for displaying exercise entries, along with their referenced exercise and its associated intensity levels.
 */
@Builder
public record ExerciseEntryResponse(
        Long id,
        Category category,
        int position,
        ExerciseResponse exerciseResponse,
        double duration,
        String level
) implements EntryResponse {
}