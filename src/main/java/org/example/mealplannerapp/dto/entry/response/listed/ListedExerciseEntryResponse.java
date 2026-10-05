package org.example.mealplannerapp.dto.entry.response.listed;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;

import java.math.BigDecimal;

/**
 * Response DTO interface for displaying {@link ExerciseEntry} data without
 * the referenced {@link Exercise} data.
 */
public record ListedExerciseEntryResponse(
        Long id,
        Category category,
        int position,
        String name,
        BigDecimal duration,
        String levelName,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) implements ListedEntryResponse {
}
