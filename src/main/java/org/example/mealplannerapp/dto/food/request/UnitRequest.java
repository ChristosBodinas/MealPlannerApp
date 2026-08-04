package org.example.mealplannerapp.dto.food.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating and updating food units.
 */
public record UnitRequest(

        @NotBlank(message = "A unit's name cannot be blank.")
        @Size(max = 20, message = "A unit's name cannot exceed 20 characters.")
        String name,

        @Positive(message = "A unit must correspond to a positive number of grams.")
        double grams
) {
}