package org.example.mealplannerapp.dto.plan;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record PlanEditRequest(
    
    @Length(min = 1, max = 45, message = "Plan name must be between 1 and 45 characters.")
    String name,

    @Positive(message = "Starting weight must be a positive number.")
    Double startWeight,

    // NOTE: Might want to gain, lose, or maintain weight.
    Double desiredChange,

    @Positive(message = "Protein ratio must be a positive number.")
    Double proteinRatio,

    @Positive(message = "Carbs ratio must be a positive number.")
    Double carbsRatio
) {
}
