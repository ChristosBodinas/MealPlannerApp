package org.example.mealplannerapp.dto.plan.request;

import org.example.mealplannerapp.common.ActivityLevel;

import jakarta.validation.constraints.*;

/**
 * Request DTO for creating new plans.
 */
public record PlanCreateRequest(

        @NotBlank(message = "Plan name cannot be blank.")
        @Size(max = 45, message = "Plan name cannot exceed 45 characters.")
        String name,

        @Positive(message = "Starting weight must be a positive number.")
        double startWeight,

        // No validation. Can be positive for weight loss, zero for maintenance, or negative for weight gain.
        double targetLoss,

        ActivityLevel activityLevel,

        // Could put a @Max here, but this will be indirectly checked on the service side anyway.
        @Positive(message = "Protein ratio must be a positive number.")
        double proteinRatio,

        @Positive(message = "Carbs ratio must be a positive number.")
        double carbsRatio,

        @Min(value = 1, message = "Plan must have at least 1 day.")
        @Max(value = 14, message = "Plan cannot be longer than 14 days.")
        int numberOfDays
) {
}
