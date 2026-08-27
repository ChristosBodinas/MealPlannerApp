package org.example.mealplannerapp.mapper;

import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.UnitRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.dto.food.response.UnitResponse;
import org.example.mealplannerapp.dto.food.response.VendorResponse;
import org.example.mealplannerapp.embeddable.ReferenceUnit;
import org.example.mealplannerapp.embeddable.VendorData;
import org.example.mealplannerapp.entity.Food;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    // HELPER METHODS
    ReferenceUnit toUnit(UnitRequest request);

    VendorData toVendor(VendorData request);

    UnitResponse toResponse(ReferenceUnit unit);

    VendorResponse toResponse(VendorData vendor);

    // MAIN METHODS
    @Mapping(target = "units", source = "units", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    @Mapping(target = "vendors", source = "vendors", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    Food toFood(FoodRequest request);

    @Mapping(target = "units", source = "units", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    @Mapping(target = "vendors", source = "vendors", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
    void update(@MappingTarget Food food, FoodRequest request);

    FoodResponse toResponse(Food food);

    ListedFoodResponse toListedResponse(Food food);
}
