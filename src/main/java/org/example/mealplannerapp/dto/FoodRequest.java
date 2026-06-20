package org.example.mealplannerapp.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

public record FoodRequest(
        @NotBlank @Length(max = 45) String name,
        @Length(max = 45) String brand,
        @PositiveOrZero double calories100g,
        @PositiveOrZero double protein100g,
        @PositiveOrZero double carbs100g,
        @PositiveOrZero double fat100g,
        @PositiveOrZero double fiber100g,
        @PositiveOrZero double purchasePrice,
        @Positive double purchaseWeight,
        @DecimalMin("0.0") @DecimalMax("1.0") double edibleRatio
) {
}
