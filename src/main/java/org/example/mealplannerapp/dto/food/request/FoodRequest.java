package org.example.mealplannerapp.dto.food.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.Set;

/**
 * Request DTO for creating and updating foods, along with their units and prices.
 */
@Builder
public record FoodRequest(

        @NotBlank(message = "A food's name cannot be blank.")
        @Size(max = 45, message = "A food's name cannot exceed 45 characters.")
        String name,

        @Size(max = 45, message = "A food's brand name cannot exceed 45 characters.")
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

        @DecimalMin(value = "0.0", inclusive = false, message = "Edible ratio must be more than 0.0%.")
        @DecimalMax(value = "1.0", inclusive = true, message = "Edible ratio cannot exceed 100.0%.")
        double edibleRatio,

        Set<@Valid UnitRequest> units,

        Set<@Valid PriceRequest> prices
) {
}