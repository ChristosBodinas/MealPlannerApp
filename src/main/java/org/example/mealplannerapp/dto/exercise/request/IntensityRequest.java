package org.example.mealplannerapp.dto.exercise.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record IntensityRequest(

    @NotBlank(message = "Exercise name cannot be blank.")
    @Size(max = 20, message = "Exercise name cannot exceed 20 characters.")
    String name,

    @Positive(message = "Calories burned per minute must be a positive number.")
    double caloriesPerMinute
) {
}
