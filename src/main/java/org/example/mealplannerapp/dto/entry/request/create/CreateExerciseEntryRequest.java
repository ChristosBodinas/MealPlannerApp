package org.example.mealplannerapp.dto.entry.request.create;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;

import java.math.BigDecimal;

/**
 * Request DTO for submitting {@link ExerciseEntry} creation data.
 */
public record CreateExerciseEntryRequest(

        @NotNull(message = "Entry category cannot be null.")
        Category category,

        @NotNull(message = "Entry must reference an existing exercise.")
        Long exerciseId,

        @NotNull(message = "Exercise duration cannot be null.")
        @Positive(message = "Exercise duration must be a positive number.")
        @Digits(integer = 3, fraction = 2, message = "Exercise duration must have at most 3 integer digits and 2 decimal places.")
        BigDecimal duration,

        @Size(max = 10, message = "Effort level name cannot exceed 10 characters.")
        String levelName
) implements CreateEntryRequest {
}
