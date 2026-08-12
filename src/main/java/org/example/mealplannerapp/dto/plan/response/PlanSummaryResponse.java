package org.example.mealplannerapp.dto.plan.response;

public record PlanSummaryResponse(
        PlanStatsResponse planStats,
        PlanGoalsResponse planGoals
) {

}
