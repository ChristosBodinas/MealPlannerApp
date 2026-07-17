package org.example.mealplannerapp.dto.entry.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import org.example.mealplannerapp.constants.Category;

@Builder
public record FoodEntryCreateRequest(

        @NotNull(message = "Entry category is required.")
        Category category,

        @NotNull(message = "Entry must contain a food.")
        Long foodId,

        @PositiveOrZero(message = "Food quantity cannot be a negative number.")
        double grams,

        // TO DO: Look into validation for these.
        String displayUnit,
        String displayMerchant
) implements EntryCreateRequest {
}
