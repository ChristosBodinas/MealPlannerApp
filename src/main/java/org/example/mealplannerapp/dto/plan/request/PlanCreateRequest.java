package org.example.mealplannerapp.dto.plan.request;

import org.example.mealplannerapp.common.ActivityLevel;

/**
 * Request DTO for creating new plans.
 */
public record PlanCreateRequest(
        // TODO: Validation
        String name,
        double startWeight,
        double targetLoss,
        ActivityLevel activityLevel,
        double proteinRatio,
        double carbsRatio,
        int numberOfDays
) {
}
