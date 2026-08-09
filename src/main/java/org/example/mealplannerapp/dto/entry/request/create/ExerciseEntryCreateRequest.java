package org.example.mealplannerapp.dto.entry.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.example.mealplannerapp.common.Category;

/**
 * Request DTO for creating exercise entries.
 */
@Builder
public record ExerciseEntryCreateRequest(

        @NotNull(message = "Entry category is required.")
        Category category,

        @NotNull(message = "Entry must contain a exercise.")
        Long exerciseId,

        @PositiveOrZero(message = "Exercise duration cannot be a negative number.")
        double duration,

        @Size(max = 20, message = "Intensity level name cannot exceed 20 characters.")
        String level
) implements EntryCreateRequest {
}