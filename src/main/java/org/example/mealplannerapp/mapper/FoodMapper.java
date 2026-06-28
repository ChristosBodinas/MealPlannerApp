package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.food.request.FoodPriceRequest;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.response.FoodPriceResponse;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.request.FoodUnitRequest;
import org.example.mealplannerapp.dto.food.response.FoodUnitResponse;
import org.example.mealplannerapp.embeddable.FoodPrice;
import org.example.mealplannerapp.embeddable.FoodUnit;
import org.example.mealplannerapp.entity.Food;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    // Main methods.
    Food createFromRequest(FoodRequest request);
    void updateFromRequest(@MappingTarget Food food, FoodRequest request);
    FoodResponse generateResponse(Food food);

    // Auxiliary methods.
    FoodUnit unitFromRequest(FoodUnitRequest request);
    FoodUnitResponse unitToResponse(FoodUnit unit);

    FoodPrice priceFromRequest(FoodPriceRequest request);
    FoodPriceResponse priceToResponse(FoodPrice price);
}
