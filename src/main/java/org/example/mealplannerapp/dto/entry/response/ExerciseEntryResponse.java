package org.example.mealplannerapp.dto.entry.response;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;

import java.math.BigDecimal;

/**
 * Response DTO interface for displaying {@link ExerciseEntry} data.
 */
public record ExerciseEntryResponse(
        Long id,
        String name,
        Category category,
        int position,
        ExerciseResponse exercise,
        BigDecimal duration,
        String levelName,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) implements EntryResponse {
}
