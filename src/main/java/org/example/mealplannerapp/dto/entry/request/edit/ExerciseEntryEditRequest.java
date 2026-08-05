package org.example.mealplannerapp.dto.entry.request.edit;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

/**
 * Request DTO for editing food entries.
 */
@Builder
public record ExerciseEntryEditRequest(
        @PositiveOrZero(message = "Exercise duration cannot be a negative number.")
        Double duration,

        // TODO: Look into validation for these.
        String intensity
) implements EntryEditRequest {
}