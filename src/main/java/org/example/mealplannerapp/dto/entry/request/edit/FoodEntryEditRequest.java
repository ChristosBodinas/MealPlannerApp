package org.example.mealplannerapp.dto.entry.request.edit;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request DTO for editing food entries.
 */
@Builder
public record FoodEntryEditRequest(
        @PositiveOrZero(message = "Food quantity cannot be a negative number.")
        Double grams,

        @Size(max = 20, message = "Unit name cannot exceed 20 characters.")
        String displayUnit,

        @Size(max = 20, message = "Vendor name cannot exceed 20 characters.")
        String selectedVendor
) implements EntryEditRequest {
}