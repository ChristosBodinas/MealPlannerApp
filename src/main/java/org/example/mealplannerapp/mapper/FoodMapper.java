package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Food toFood(FoodRequest request);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    void update(@MappingTarget Food food, FoodRequest request);

    FoodResponse toResponse(Food food);

    ListedFoodResponse toListedResponse(Food food);
}
