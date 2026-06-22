package org.example.mealplannerapp.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

public record FoodRequest(
        @NotBlank(message = "Name cannot be blank.")
        @Length(max = 45, message = "Name cannot be longer than 45 characters.")
        String name,

        @Length(max = 45, message = "Brand cannot be longer than 45 characters.")
        String brand,

        @PositiveOrZero(message = "Calories cannot be a negative number.")
        double calories100g,

        @PositiveOrZero(message = "Protein cannot be a negative number.")
        double protein100g,

        @PositiveOrZero(message = "Carbs cannot be a negative number.")
        double carbs100g,

        @PositiveOrZero(message = "Fat cannot be a negative number.")
        double fat100g,

        @PositiveOrZero(message = "Fiber cannot be a negative number.")
        double fiber100g,

        @PositiveOrZero(message = "Purchase price cannot be a negative number.")
        double purchasePrice,

        @Positive (message = "Purchase weight must be a positive number.")
        double purchaseWeight,

        @DecimalMin(value = "0.0", inclusive = false, message = "Edible ratio cannot be less than 0%.")
        @DecimalMax(value = "1.0", message = "Edible ratio cannot be less than 100%.")
        double edibleRatio
) {
}
