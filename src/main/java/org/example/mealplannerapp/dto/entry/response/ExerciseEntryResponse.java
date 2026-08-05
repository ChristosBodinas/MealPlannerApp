package org.example.mealplannerapp.dto.entry.response;

import lombok.Builder;
import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.dto.exercise.response.ExerciseEntry;

/**
 * Response DTO for displaying food entries, along with their reference food and its associated units/prices.
 */
@Builder
public record ExerciseEntryResponse(
        Long id,
        Category category,
        int position,
        ExerciseResponse exerciseResponse,
        double duration,
        String intensity
) implements EntryResponse {
}