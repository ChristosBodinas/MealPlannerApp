package org.example.mealplannerapp.dto.food.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record FoodUnitRequest(

        @NotBlank(message = "Unit name cannot be blank.")
        String name,

        @Positive(message = "Grams per unit must be a positive number.")
        double grams
) {
}
