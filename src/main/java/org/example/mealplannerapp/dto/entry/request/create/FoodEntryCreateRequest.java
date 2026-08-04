package org.example.mealplannerapp.dto.entry.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.example.mealplannerapp.common.Category;

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

        @Size(max = 20, message = "Unit name cannot exceed 20 characters.")
        String unit,

        @Size(max = 20, message = "Vendor name cannot exceed 20 characters.")
        String vendor
) implements EntryCreateRequest {
}