package org.example.mealplannerapp.dto.entry.request.edit;

import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request DTO for editing food entries.
 */
public record FoodEntryEditRequest(
        @PositiveOrZero(message = "Food quantity cannot be a negative number.")
        Double grams,

        // TODO: Look into validation for these.
        String displayUnit,

        String displayMerchant
) implements EntryEditRequest {
}