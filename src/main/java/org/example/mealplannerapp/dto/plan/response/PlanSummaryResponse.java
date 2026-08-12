package org.example.mealplannerapp.dto.plan.response;

import java.util.List;

public record PlanSummaryResponse(
    PlanStatsResponse planStats,
    PlanGoalsResponse planGoals
) {
    
}
