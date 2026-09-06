package org.example.mealplannerapp.dto.entry.request.create;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.FoodEntry;

import java.math.BigDecimal;

/**
 * Request DTO for submitting {@link FoodEntry} creation data.
 */
public record CreateFoodEntryRequest(

        @NotNull(message = "Entry category cannot be null.")
        Category category,

        @NotNull(message = "Entry must reference an existing food.")
        Long foodId,

        @NotNull(message = "Food quantity cannot be null.")
        @Positive(message = "Food quantity must be a positive number.")
        @Digits(integer = 4, fraction = 2, message = "Food quantity must have at most 4 integer digits and 2 decimal places.")
        BigDecimal grams,

        @Size(max = 10, message = "Reference unit name cannot exceed 10 characters.")
        String unitName,

        @Size(max = 10, message = "Vendor name cannot exceed 10 characters.")
        String vendorName
) implements CreateEntryRequest {
}
