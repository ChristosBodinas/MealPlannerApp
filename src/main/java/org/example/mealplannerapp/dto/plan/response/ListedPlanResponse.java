package org.example.mealplannerapp.dto.plan.response;

import org.example.mealplannerapp.common.ActivityLevel;

public record ListedPlanResponse(
        Long id,
        String name,
        double startWeight,
        double targetLoss,
        ActivityLevel activityLevel,
        double targetCalories,
        double targetProtein,
        double targetCarbs,
        double targetFat,
        double targetFiber
) {
}
