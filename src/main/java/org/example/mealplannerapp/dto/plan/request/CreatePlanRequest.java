package org.example.mealplannerapp.dto.plan.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import org.example.mealplannerapp.common.ActivityLevel;
import org.example.mealplannerapp.entity.Plan;

import java.math.BigDecimal;

/**
 * Request DTO for submitting {@link Plan} creation data.
 */
@Builder
public record CreatePlanRequest(

        @NotBlank(message = "Plan name must contain at least 1 character.")
        @Size(max = 50, message = "Plan name cannot exceed 50 characters.")
        String name,

        @NotNull(message = "Starting weight cannot be null.")
        @Positive(message = "Starting weight must be a positive number.")
        @Digits(integer = 3, fraction = 2, message = "Starting weight must have at most 3 integer digits and 2 decimal places.")
        BigDecimal startWeight,

        @NotNull(message = "Desired weight loss cannot be null.")
        @Digits(integer = 1, fraction = 2, message = "Desired weight loss must have at most 1 integer digit and 2 decimal places.")
        BigDecimal desiredWeightLoss,

        @NotNull(message = "Activity level cannot be null.")
        ActivityLevel activityLevel,

        @NotNull(message = "Protein ratio cannot be null.")
        @DecimalMin(value = "0.0", inclusive = false, message = "Protein ratio must be a positive number.")
        @DecimalMax(value = "1.0", inclusive = false, message = "Protein ratio must be less than 1.")
        @Digits(integer = 1, fraction = 2, message = "Protein ratio must have at most 1 integer digit and 2 decimal places.")
        BigDecimal proteinRatio,

        @NotNull(message = "Carbs ratio cannot be null.")
        @DecimalMin(value = "0.0", inclusive = false, message = "Carbs ratio must be a positive number.")
        @DecimalMax(value = "1.0", inclusive = false, message = "Carbs ratio must be less than 1.")
        @Digits(integer = 1, fraction = 2, message = "Carbs ratio must have at most 1 integer digit and 2 decimal places.")
        BigDecimal carbsRatio,

        @NotNull(message = "Fat ratio cannot be null.")
        @DecimalMin(value = "0.0", inclusive = false, message = "Fat ratio must be a positive number.")
        @DecimalMax(value = "1.0", inclusive = false, message = "Fat ratio must be less than 1.")
        @Digits(integer = 1, fraction = 2, message = "Fat ratio must have at most 1 integer digit and 2 decimal places.")
        BigDecimal fatRatio,

        @Min(value = 1, message = "Plan must contain at least 1 day.")
        @Max(value = 14, message = "Plan cannot contain more than 14 days.")
        int numberOfDays
) {
}
