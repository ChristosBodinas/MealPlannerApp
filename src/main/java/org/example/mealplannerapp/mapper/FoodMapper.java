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
    FoodUnit unitFromRequest(UnitRequest request);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Set<FoodUnit> unitsFromRequests(Set<UnitRequest> requests);

    UnitResponse responseFromUnit(FoodUnit unit);

    // FOOD PRICE MAPPING
    FoodPrice priceFromRequest(PriceRequest request);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Set<FoodPrice> pricesFromRequests(Set<PriceRequest> requests);

    PriceResponse responseFromPrice(FoodPrice price);

    // FOOD MAPPING
    @Mapping(target = "user", ignore = true)
    Food createFromRequest(FoodRequest request);

    @Mapping(target = "user", ignore = true)
    void updateFromRequest(@MappingTarget Food food, FoodRequest request);

    FoodResponse generateResponse(Food food);

    ListedFoodResponse generateListedResponse(Food food);
}
