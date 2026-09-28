package org.example.mealplannerapp.dto.entry.response;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;

import java.math.BigDecimal;

/**
 * Response DTO for displaying {@link ExerciseEntry} data
 * along with the referenced {@link Exercise} data.
 */
public record ExerciseEntryResponse(
        Long id,
        Category category,
        int position,
        ExerciseResponse exercise,
        String name,
        FoodResponse food,
        BigDecimal calories,
        BigDecimal protein,
        BigDecimal carbs,
        BigDecimal fat,
        BigDecimal fiber,
        BigDecimal price
) implements EntryResponse {
}
