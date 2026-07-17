package org.example.mealplannerapp.dto.entry.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.mealplannerapp.constants.Category;

public record EntryMoveRequest(

        @NotNull(message = "A target category for the move must be provided.")
        Category category,

        @Positive(message = "Target position must be a positive number.")
        int desiredPosition
) {
}
