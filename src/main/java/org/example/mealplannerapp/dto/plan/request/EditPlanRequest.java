package org.example.mealplannerapp.dto.plan.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import org.example.mealplannerapp.common.ActivityLevel;
import org.example.mealplannerapp.entity.Plan;

import java.math.BigDecimal;

/**
 * Request DTO for submitted {@link Plan} update data.
 */
@Builder
public record EditPlanRequest(

        @NotNull(message = "Plan name cannot be null.")
        @Size(min = 1, max = 50, message = "Plan name must be between 1 and 50 characters..")
        String name,

        @Positive(message = "Starting weight must be a positive number.")
        @Digits(integer = 3, fraction = 2, message = "Starting weight must have at most 3 integer digits and 2 decimal places.")
        BigDecimal startWeight,

        @Digits(integer = 1, fraction = 2, message = "Desired weight loss must have at most 1 integer digit and 2 decimal places.")
        BigDecimal desiredWeightLoss,

        ActivityLevel activityLevel,

        @DecimalMin(value = "0.0", inclusive = false, message = "Protein ratio must be a positive number.")
        @DecimalMax(value = "1.0", inclusive = false, message = "Protein ratio must be less than 1.")
        @Digits(integer = 1, fraction = 2, message = "Protein ratio must have at most 1 integer digit and 2 decimal places.")
        BigDecimal proteinRatio,

        @DecimalMin(value = "0.0", inclusive = false, message = "Carbs ratio must be a positive number.")
        @DecimalMax(value = "1.0", inclusive = false, message = "Carbs ratio must be less than 1.")
        @Digits(integer = 1, fraction = 2, message = "Carbs ratio must have at most 1 integer digit and 2 decimal places.")
        BigDecimal carbsRatio,

        @DecimalMin(value = "0.0", inclusive = false, message = "Fat ratio must be a positive number.")
        @DecimalMax(value = "1.0", inclusive = false, message = "Fat ratio must be less than 1.")
        @Digits(integer = 1, fraction = 2, message = "Fat ratio must have at most 1 integer digit and 2 decimal places.")
        BigDecimal fatRatio
) {
}
