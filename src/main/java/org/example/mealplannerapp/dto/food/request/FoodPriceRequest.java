package org.example.mealplannerapp.dto.food.request;

import jakarta.validation.constraints.*;

public record FoodPriceRequest (

        @NotBlank(message = "Seller name cannot be empty.")
        String seller,

        @PositiveOrZero(message = "Purchase price cannot be a negative number.")
        double purchasePrice,

        @Positive(message = "Purchase quantity must be a positive number.")
        double purchaseGrams
) {
}
