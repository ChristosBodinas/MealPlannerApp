package org.example.mealplannerapp.dto.food.request;

import jakarta.validation.constraints.*;
import org.example.mealplannerapp.embeddable.ReferenceUnit;

import java.math.BigDecimal;

/**
 * Request DTO for submitting {@link ReferenceUnit} creation/update data.
 */
public record UnitRequest(

        @NotBlank(message = "Reference unit name cannot be blank.")
        @Size(max = 10, message = "Reference unit name cannot exceed 10 characters.")
        String name,

        @NotNull(message = "Equivalent grams cannot be null.")
        @Positive(message = "Equivalent grams must be a positive number.")
        @Digits(integer = 3, fraction = 2, message = "Equivalent grams cannot have more than 3 integer digits and 2 decimal places.")
        BigDecimal grams
) {
}
