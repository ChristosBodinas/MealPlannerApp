package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.plan.request.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.request.PlanEditRequest;
import org.example.mealplannerapp.dto.plan.response.*;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.projection.Goals;
import org.example.mealplannerapp.projection.Stats;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = DayMapper.class)
public interface PlanMapper {

    Plan toPlan(PlanCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget Plan plan, PlanEditRequest request);

    PlanResponse toResponse(Plan plan);

    ListedPlanResponse toListedResponse(Plan plan);

    PlanStatsResponse toStatsResponse(Stats planStats);

    PlanGoalsResponse toGoalsResponse(Goals planGoals);

    PlanSummaryResponse toSummaryResponse(Stats planStats, Goals planGoals);

}
