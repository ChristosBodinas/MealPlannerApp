package org.example.mealplannerapp.dto.plan.response;

import org.example.mealplannerapp.common.ActivityLevel;
import org.example.mealplannerapp.dto.day.response.DayResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Response DTO for displaying {@link Plan} and
 * associated {@link Day} data.
 */
public record PlanResponse(
        Long id,
        String name,
        BigDecimal startWeight,
        BigDecimal desiredWeightLoss,
        ActivityLevel activityLevel,
        BigDecimal targetCalories,
        BigDecimal targetProtein,
        BigDecimal targetCarbs,
        BigDecimal targetFat,
        Set<DayResponse> days
) {
}
