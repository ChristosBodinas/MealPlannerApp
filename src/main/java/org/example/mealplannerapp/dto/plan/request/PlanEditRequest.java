package org.example.mealplannerapp.dto.plan.request;

import org.example.mealplannerapp.common.ActivityLevel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for editing plan parameters.
 */
public record PlanEditRequest(

        @NotBlank(message = "Plan name cannot be blank.")
        @Size(max = 45, message = "Plan name cannot exceed 45 characters.")
        String name,

        @Positive(message = "Starting weight must be a positive number.")
        Double startWeight,

        // No validation. Can be positive for weight loss, zero for maintenance, or negative for weight gain.
        Double targetLoss,

        ActivityLevel activityLevel,

        // Could put a @Max here, but this will be indirectly checked on the service side anyway.
        @Positive(message = "Protein ratio must be a positive number.")
        Double proteinRatio,

        @Positive(message = "Carbs ratio must be a positive number.")
        Double carbsRatio
) {
}
