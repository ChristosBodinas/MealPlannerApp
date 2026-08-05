package org.example.mealplannerapp.dto.entry.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import org.example.mealplannerapp.constants.Category;

/**
 * Request DTO for creating food entries.
 */
@Builder
public record ExerciseEntryCreateRequest(

        @NotNull(message = "Entry category is required.")
        Category category,

        @NotNull(message = "Entry must contain an exercise.")
        Long exerciseId,

        @PositiveOrZero(message = "Exercise duration cannot be a negative number.")
        double duration,

        // TODO: Look into validation for these.
        String intensity
) implements EntryCreateRequest {
}