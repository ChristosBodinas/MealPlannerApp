package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.food.request.FoodPriceRequest;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    // FOOD UNIT MAPPING
    FoodUnit unitFromRequest(FoodUnitRequest request);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Set<FoodUnit> unitsFromRequests(Set<FoodUnitRequest> requests);
    
    FoodUnitResponse responseFromUnit(FoodUnit unit); 

    // FOOD PRICE MAPPING
    FoodPrice priceFromRequest(FoodPriceRequest request);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    Set<FoodPrice> pricesFromRequests(Set<FoodPriceRequest> requests);

    FoodPriceResponse responseFromPrice(FoodPrice price);
    
    // FOOD MAPPING
    Food createFromRequest(FoodRequest request);

    void updateFromRequest(@MappingTarget Food food, FoodRequest request);

    FoodResponse generateResponse(Food food);
}
