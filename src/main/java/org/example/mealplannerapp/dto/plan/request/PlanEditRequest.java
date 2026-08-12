package org.example.mealplannerapp.dto.plan.request;

import org.example.mealplannerapp.common.ActivityLevel;

/**
 * Request DTO for editing plan parameters.
 */
public record PlanEditRequest(
        // TODO: Validation
        String name,
        Double startWeight,
        Double targetLoss,
        ActivityLevel activityLevel,
        Double proteinRatio,
        Double carbsRatio
) {
}
