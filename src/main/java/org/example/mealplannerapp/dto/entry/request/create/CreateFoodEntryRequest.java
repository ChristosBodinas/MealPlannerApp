package org.example.mealplannerapp.dto.entry.request.create;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.FoodEntry;

import java.math.BigDecimal;

/**
 * Request DTO for submitting {@link FoodEntry} creation data.
 */
@Builder
public record CreateFoodEntryRequest(

        @NotNull(message = "Entry category cannot be null.")
        Category category,

        @NotNull(message = "Referenced food ID cannot be null.")
        Long foodId,

        @NotNull(message = "Food quantity cannot be null.")
        @PositiveOrZero(message = "Food quantity cannot be a negative number.")
        @Digits(integer = 4, fraction = 2, message = "Food quantity must have at most 4 integer digits and 2 decimal places.")
        BigDecimal quantity,

        // No validation. Nulls are acceptable, and invalid strings are handled by the service.
        String unitName,

        // No validation. Nulls are acceptable, and invalid strings are handled by the service.
        String vendorName
) implements CreateEntryRequest {
}
