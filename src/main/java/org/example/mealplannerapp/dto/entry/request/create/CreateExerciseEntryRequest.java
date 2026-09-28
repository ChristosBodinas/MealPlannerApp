package org.example.mealplannerapp.dto.entry.request.create;


import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;

import java.math.BigDecimal;

/**
 * Request DTO for submitting {@link ExerciseEntry} creation data.
 */
@Builder
public record CreateExerciseEntryRequest(

        @NotNull(message = "Entry category cannot be null.")
        Category category,

        @NotNull(message = "Referenced exercise ID cannot be null.")
        Long exerciseId,

        @NotNull(message = "Exercise duration cannot be null.")
        @PositiveOrZero(message = "Exercise duration cannot be a negative number.")
        @Digits(integer = 3, fraction = 2, message = "Exercise duration must have at most 3 integer digits and 2 decimal places.")
        BigDecimal duration,

        // No validation. Null is acceptable, and invalid strings are handled by the service.
        String levelName
) implements CreateEntryRequest {
}
