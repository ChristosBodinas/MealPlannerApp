package org.example.mealplannerapp.dto.entry.request.edit;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request DTO for editing exercise entries.
 */
@Builder
public record ExerciseEntryEditRequest(
        @PositiveOrZero(message = "Exercise duration cannot be a negative number.")
        Double duration,

        @Size(max = 20, message = "Intensity level name cannot exceed 20 characters.")
        String level
) implements EntryEditRequest {
}