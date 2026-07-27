package org.example.mealplannerapp.dto.entry.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import org.example.mealplannerapp.constants.Category;

/**
 * Request DTO for creating food entries.
 */
@Builder
public record FoodEntryCreateRequest(

        @NotNull(message = "Entry category is required.")
        Category category,

        @NotNull(message = "Entry must contain a food.")
        Long foodId,

        @PositiveOrZero(message = "Food quantity cannot be a negative number.")
        double grams,

        // TODO: Look into validation for these.
        String displayUnit,

        String selectedVendor
) implements EntryCreateRequest {
}