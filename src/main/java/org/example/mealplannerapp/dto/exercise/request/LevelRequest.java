package org.example.mealplannerapp.dto.exercise.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LevelRequest(

        @NotBlank(message = "Intensity level name cannot be blank.")
        @Size(max = 20, message = "Intensity level name cannot exceed 20 characters.")
        String name,

        @Positive(message = "Calories burned per minute must be a positive number.")
        double caloriesPerMinute
) {
}