package org.example.mealplannerapp.dto.food.request;

import jakarta.validation.constraints.*;
import org.example.mealplannerapp.embeddable.VendorData;

import java.math.BigDecimal;

/**
 * Request DTO for submitting {@link VendorData} creation/update data.
 */
public record VendorRequest(

        @NotBlank(message = "Vendor name cannot be blank.")
        @Size(max = 10, message = "Vendor name cannot exceed 10 characters.")
        String name,

        @NotNull(message = "Purchase price cannot be null.")
        @PositiveOrZero(message = "Purchase price cannot be a negative number.")
        // Zero allows for foods the user can get for free.
        @Digits(integer = 3, fraction = 2, message = "Purchase price cannot have more than 3 integer digits and 2 decimal places.")
        BigDecimal purchasePrice,

        @NotNull(message = "Purchase grams cannot be null.")
        @Positive(message = "Purchase grams must be a positive number.")
        @Digits(integer = 3, fraction = 2, message = "Purchase grams cannot have more than 3 integer digits and 2 decimal places.")
        BigDecimal purchaseGrams
) {
}
