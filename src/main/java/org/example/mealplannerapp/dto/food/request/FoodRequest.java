package org.example.mealplannerapp.dto.food.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record FoodRequest(
        @NotBlank(message = "Name cannot be blank.")
        @Length(max = 45, message = "Name cannot be longer than 45 characters.")
        String name,

        @Length(max = 45, message = "Brand cannot be longer than 45 characters.")
        String brand,

        @PositiveOrZero(message = "Calories cannot be a negative number.")
        double caloriesPer100g,

        @PositiveOrZero(message = "Protein cannot be a negative number.")
        double proteinPer100g,

        @PositiveOrZero(message = "Carbs cannot be a negative number.")
        double carbsPer100g,

        @PositiveOrZero(message = "Fat cannot be a negative number.")
        double fatPer100g,

        @PositiveOrZero(message = "Fiber cannot be a negative number.")
        double fiberPer100g,

        @DecimalMin(value = "0.0", inclusive = false, message = "Edible ratio cannot be less than 0%.")
        @DecimalMax(value = "1.0", message = "Edible ratio cannot be greater than 100%.")
        double edibleRatio,

        Set<@Valid FoodUnitRequest> units,

        Set<@Valid FoodPriceRequest> prices
) {
}
