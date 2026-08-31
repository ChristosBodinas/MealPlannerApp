package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.plan.request.CreatePlanRequest;
import org.example.mealplannerapp.dto.plan.request.EditPlanRequest;
import org.example.mealplannerapp.dto.plan.response.ListedPlanResponse;
import org.example.mealplannerapp.dto.plan.response.PlanResponse;
import org.example.mealplannerapp.entity.Plan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = DayMapper.class)
public interface PlanMapper {

    Plan toPlan(CreatePlanRequest request);

    // These ratios must be either all null or all non-null, so they will be handled on the service.
    @Mapping(target = "proteinRatio", ignore = true)
    @Mapping(target = "carbsRatio", ignore = true)
    @Mapping(target = "fatRatio", ignore = true)
    void update(@MappingTarget Plan plan, EditPlanRequest request);

    PlanResponse toResponse(Plan plan);

    ListedPlanResponse toListedResponse(Plan plan);
}
