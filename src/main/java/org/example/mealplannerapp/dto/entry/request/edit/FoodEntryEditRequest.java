package org.example.mealplannerapp.dto.entry.request.edit;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

/**
 * Request DTO for editing food entries.
 */
@Builder
public record FoodEntryEditRequest(
        @PositiveOrZero(message = "Food quantity cannot be a negative number.")
        Double grams,

        // TODO: Look into validation for these.
        String displayUnit,

        String selectedVendor
) implements EntryEditRequest {
}