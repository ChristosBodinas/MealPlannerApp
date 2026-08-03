package org.example.mealplannerapp.dto.food.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

/**
 * Request DTO for creating and updating food prices.
 */
public record PriceRequest(

        @NotBlank(message = "A vendor's name cannot be blank.")
        @Length(max = 20, message = "A vendor's name cannot exceed 20 characters.")
        String vendor,

        @PositiveOrZero(message = "Purchase price cannot be a negative number.")
        double purchasePrice,

        @Positive(message = "Purchase quantity must be a positive number.")
        double purchaseGrams
) {
}