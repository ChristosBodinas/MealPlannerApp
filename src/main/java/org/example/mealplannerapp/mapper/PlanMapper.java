package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.plan.request.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.request.PlanEditRequest;
import org.example.mealplannerapp.dto.plan.response.ListedPlanResponse;
import org.example.mealplannerapp.dto.plan.response.PlanGoalsResponse;
import org.example.mealplannerapp.dto.plan.response.PlanResponse;
import org.example.mealplannerapp.dto.plan.response.PlanStatsResponse;
import org.example.mealplannerapp.dto.plan.response.PlanSummaryResponse;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.projection.Goals;
import org.example.mealplannerapp.projection.Stats;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = DayMapper.class)
public interface PlanMapper {

    Plan toPlan(PlanCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Plan plan, PlanEditRequest request);

    PlanResponse toResponse(Plan plan);

    ListedPlanResponse toListedResponse(Plan plan);

    PlanStatsResponse toStatsResponse(Stats planStats);
    PlanGoalsResponse toGoalsResponse(Goals planGoals);
    
    PlanSummaryResponse toSummaryResponse(Stats planGoals, Goals planGoals);
    
}
