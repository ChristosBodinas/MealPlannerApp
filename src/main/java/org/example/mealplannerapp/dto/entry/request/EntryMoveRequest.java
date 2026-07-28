package org.example.mealplannerapp.dto.entry.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import org.example.mealplannerapp.constants.Category;

/**
 * Request DTO for moving entries.
 */
@Builder
public record EntryMoveRequest(

        @NotNull(message = "A target category for the move must be provided.")
        Category category,

        @Positive(message = "Target position must be a positive number.")
        int desiredPosition
) {
}