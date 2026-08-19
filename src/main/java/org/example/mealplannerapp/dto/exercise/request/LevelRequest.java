package org.example.mealplannerapp.dto.exercise.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

public record LevelRequest(

    @NotBlank(message = "Intensity description must contain at least 1 character.")
    @Size(max = 10, message = "Intensity description cannot exceed 10 characters.")
    String intensityDesc,
    
    @NotNull(message = "Calories burned per minute cannot be null.")
    @Positive(message = "Calories burned per minute must be a positive number.")
    @Digits(integer = 3, fraction = 2, message = "Calories burned per minute must have at most 3 integer digits and 2 decimal digits.")
    BigDecimal caloriesPerMinute
) {
    
}
