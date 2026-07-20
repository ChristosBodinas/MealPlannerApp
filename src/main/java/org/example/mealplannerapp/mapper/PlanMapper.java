package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.projection.PlanNutrients;
import org.example.mealplannerapp.dto.plan.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.PlanEditRequest;
import org.example.mealplannerapp.dto.plan.PlanInfoResponse;
import org.example.mealplannerapp.dto.plan.PlanNutrientsResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PlanMapper {

    Plan createFromRequest(PlanCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromRequest(@MappingTarget Plan plan, PlanEditRequest request);

    PlanInfoResponse generateResponse(Plan plan);
    PlanNutrientsResponse generateNutrientsResponse(PlanNutrients nutrients);
    
}
