package org.example.mealplannerapp.dto.plan;

import lombok.Builder;

@Builder
public record PlanInfoResponse(
    Long id,
    String name,
    double startWeight,
    double desiredChange,
    double proteinRatio,
    double carbsRatio
) {
}
