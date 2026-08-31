package org.example.mealplannerapp.dto.food.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import org.example.mealplannerapp.entity.Food;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Request DTO for submitting {@link Food} creation/update data.
 */
@Builder
public record FoodRequest(

        @NotBlank(message = "Food name must contain at least 1 character.")
        @Size(max = 50, message = "Food name cannot exceed 50 characters.")
        String name,

        @Size(max = 20, message = "Brand name cannot exceed 20 characters.")
        String brand,

        @NotNull(message = "Calories per 100 grams cannot be null.")
        @PositiveOrZero(message = "Calories per 100 grams cannot be a negative number.")
        @Digits(integer = 3, fraction = 2, message = "Calories per 100 grams must have at most 3 integer digits and 2 decimal places.")
        BigDecimal calories100g,

        @NotNull(message = "Protein per 100 grams cannot be null.")
        @PositiveOrZero(message = "Protein per 100 grams cannot be a negative number.")
        @Digits(integer = 3, fraction = 2, message = "Protein per 100 grams must have at most 3 integer digits and 2 decimal places.")
        BigDecimal protein100g,

        @NotNull(message = "Carbs per 100 grams cannot be null.")
        @PositiveOrZero(message = "Carbs per 100 grams cannot be a negative number.")
        @Digits(integer = 3, fraction = 2, message = "Carbs per 100 grams must have at most 3 integer digits and 2 decimal places.")
        BigDecimal carbs100g,

        @NotNull(message = "Fat per 100 grams cannot be null.")
        @PositiveOrZero(message = "Fat per 100 grams cannot be a negative number.")
        @Digits(integer = 3, fraction = 2, message = "Fat per 100 grams must have at most 3 integer digits and 2 decimal places.")
        BigDecimal fat100g,

        @NotNull(message = "Fiber per 100 grams cannot be null.")
        @PositiveOrZero(message = "Fiber per 100 grams cannot be a negative number.")
        @Digits(integer = 3, fraction = 2, message = "Fiber per 100 grams must have at most 3 integer digits and 2 decimal places.")
        BigDecimal fiber100g,

        @NotNull(message = "Edible ratio cannot be null.")
        @DecimalMin(value = "0.00", message = "Edible ratio cannot be less than 0.")
        @DecimalMax(value = "1.00", message = "Edible ratio cannot be greater than 1.")
        @Digits(integer = 1, fraction = 2, message = "Edible ratio must have at most 1 integer digit and 2 decimal places.")
        BigDecimal edibleRatio,

        Set<@Valid UnitRequest> units,

        Set<@Valid VendorRequest> vendors
) {
}
