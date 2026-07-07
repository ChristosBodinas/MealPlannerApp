package org.example.mealplannerapp.dto.food.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

public record FoodUnitRequest(

        @NotBlank(message = "A unit's name cannot be blank.")
        @Length(max = 45, message = "A unit's name cannot exceed 45 characters.")
        String name,

        @Positive(message = "A unit must correspond to a positive number of grams.")
        double grams
) {
}
