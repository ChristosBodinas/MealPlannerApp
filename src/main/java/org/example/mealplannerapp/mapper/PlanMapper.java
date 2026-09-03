package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.plan.request.CreatePlanRequest;
import org.example.mealplannerapp.dto.plan.request.EditPlanRequest;
import org.example.mealplannerapp.dto.plan.response.ListedPlanResponse;
import org.example.mealplannerapp.dto.plan.response.PlanResponse;
import org.example.mealplannerapp.entity.Plan;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = DayMapper.class,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface PlanMapper {

    Plan toPlan(CreatePlanRequest request);

    @Mapping(target = "proteinRatio", ignore = true)
    @Mapping(target = "carbsRatio", ignore = true)
    @Mapping(target = "fatRatio", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateExcludeRatios(@MappingTarget Plan plan, EditPlanRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateIncludeRatios(@MappingTarget Plan plan, EditPlanRequest request);

    PlanResponse toResponse(Plan plan);

    ListedPlanResponse toListedResponse(Plan plan);
}
