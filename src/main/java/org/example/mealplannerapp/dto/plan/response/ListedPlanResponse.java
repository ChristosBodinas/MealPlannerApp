package org.example.mealplannerapp.dto.plan.response;

import org.example.mealplannerapp.common.ActivityLevel;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;

import java.math.BigDecimal;

/**
 * Response DTO for displaying {@link Plan} data without the
 * associated {@link Day} data.
 * Intended for listing multiple exercises.
 */
public record ListedPlanResponse(
        Long id,
        String name,
        BigDecimal startWeight,
        BigDecimal desiredWeightLoss,
        ActivityLevel activityLevel,
        BigDecimal targetCalories,
        BigDecimal targetProtein,
        BigDecimal targetCarbs,
        BigDecimal targetFat
) {
}
