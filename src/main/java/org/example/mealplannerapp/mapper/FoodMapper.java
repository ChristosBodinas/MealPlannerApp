package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.PriceRequest;
import org.example.mealplannerapp.dto.food.request.UnitRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.dto.food.response.PriceResponse;
import org.example.mealplannerapp.dto.food.response.UnitResponse;
import org.example.mealplannerapp.embeddable.FoodPrice;
import org.example.mealplannerapp.embeddable.FoodUnit;
import org.example.mealplannerapp.entity.Food;
import org.mapstruct.*;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    // FOOD UNIT MAPPING
    FoodUnit toUnit(UnitRequest request);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Set<FoodUnit> toUnits(Set<UnitRequest> requests);

    UnitResponse toUnitResponse(FoodUnit unit);

    // FOOD PRICE MAPPING
    FoodPrice toPrice(PriceRequest request);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Set<FoodPrice> toPrices(Set<PriceRequest> requests);

    PriceResponse toPriceResponse(FoodPrice price);

    // FOOD MAPPING
    @Mapping(target = "user", ignore = true)
    Food toFood(FoodRequest request);

    @Mapping(target = "user", ignore = true)
    void update(@MappingTarget Food food, FoodRequest request);

    FoodResponse toResponse(Food food);

    ListedFoodResponse toListedResponse(Food food);
}