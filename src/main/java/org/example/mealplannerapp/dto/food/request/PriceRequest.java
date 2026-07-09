package org.example.mealplannerapp.dto.food.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

public record PriceRequest(

        @NotBlank(message = "A merchant's name cannot be blank.")
        @Length(max = 45, message = "A merchant's name cannot exceed 45 characters.")
        String merchant,

        @PositiveOrZero(message = "Purchase price cannot be a negative number.")
        double purchasePrice,

        @Positive(message = "Purchase quantity must be a positive number.")
        double purchaseGrams
) {
}
