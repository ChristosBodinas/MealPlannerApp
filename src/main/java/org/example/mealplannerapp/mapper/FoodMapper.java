package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.FoodRequest;
import org.example.mealplannerapp.dto.FoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FoodMapper {
    Food createFromRequest(FoodRequest request);
    void updateFromRequest(@MappingTarget Food food, FoodRequest request);
    FoodResponse generateResponse(Food food);
}
