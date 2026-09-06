package org.example.mealplannerapp.dto.entry.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.Entry;

/**
 * Request DTO for submitting {@link Entry} reordering data.
 */
public record MoveEntryRequest(

        @NotNull(message = "A target category must be provided for the move.")
        Category category,

        @Positive(message = "Target position must be a positive number.")
        int position
) {
}
