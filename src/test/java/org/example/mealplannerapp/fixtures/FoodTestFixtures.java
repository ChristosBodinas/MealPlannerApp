package org.example.mealplannerapp.fixtures;

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

import java.util.Set;

public class FoodTestFixtures {

    private static final Long DEFAULT_ID = 1L;
    private static final String DEFAULT_NAME = "Fake Food";
    private static final String DEFAULT_BRAND = "Fake Brand";
    private static final double DEFAULT_CALORIES_PER_100G = 97.0;
    private static final double DEFAULT_PROTEIN_PER_100G = 12.0;
    private static final double DEFAULT_CARBS_PER_100G = 37.5;
    private static final double DEFAULT_FAT_PER_100G = 4.5;
    private static final double DEFAULT_FIBER_PER_100G = 6.0;
    private static final double DEFAULT_EDIBLE_RATIO = 1.0;

    /**
     * Fixture for building {@link Food} entities.
     * @return a Food builder with default values for all fields except {@code user}
     */
    public static Food.FoodBuilder defaultFoodBuilder() {
        return Food.builder()
                .id(DEFAULT_ID)
                .name(DEFAULT_NAME)
                .brand(DEFAULT_BRAND)
                .caloriesPer100g(DEFAULT_CALORIES_PER_100G)
                .proteinPer100g(DEFAULT_PROTEIN_PER_100G)
                .carbsPer100g(DEFAULT_CARBS_PER_100G)
                .fatPer100g(DEFAULT_FAT_PER_100G)
                .fiberPer100g(DEFAULT_FIBER_PER_100G)
                .edibleRatio(DEFAULT_EDIBLE_RATIO)
                .units(Set.of(
                        new FoodUnit("tbsp", 15.0),
                        new FoodUnit("cup", 235.0)))
                .prices(Set.of(
                        new FoodPrice("Masoutis", 6.80, 200),
                        new FoodPrice("MyMarket", 5.70, 175)));
    }

    /**
     * Method for building {@link FoodRequest} DTOs.
     * @return a FoodRequest builder with default values for all fields.
     */
    public static FoodRequest.FoodRequestBuilder defaultFoodRequestBuilder() {
        return FoodRequest.builder()
                .name(DEFAULT_NAME)
                .brand(DEFAULT_BRAND)
                .caloriesPer100g(DEFAULT_CALORIES_PER_100G)
                .proteinPer100g(DEFAULT_PROTEIN_PER_100G)
                .carbsPer100g(DEFAULT_CARBS_PER_100G)
                .fatPer100g(DEFAULT_FAT_PER_100G)
                .fiberPer100g(DEFAULT_FIBER_PER_100G)
                .edibleRatio(DEFAULT_EDIBLE_RATIO)
                .units(Set.of(
                        new UnitRequest("tbsp", 15.0),
                        new UnitRequest("cup", 235.0)))
                .prices(Set.of(
                        new PriceRequest("Masoutis", 6.80, 200),
                        new PriceRequest("MyMarket", 5.70, 175)));
    }

    /**
     * Method for building {@link FoodResponse} DTOs.
     * @return a FoodResponse builder with default values for all fields.
     */
    public static FoodResponse.FoodResponseBuilder defaultFoodResponseBuilder() {
        return FoodResponse.builder()
                .id(DEFAULT_ID)
                .name(DEFAULT_NAME)
                .brand(DEFAULT_BRAND)
                .caloriesPer100g(DEFAULT_CALORIES_PER_100G)
                .proteinPer100g(DEFAULT_PROTEIN_PER_100G)
                .carbsPer100g(DEFAULT_CARBS_PER_100G)
                .fatPer100g(DEFAULT_FAT_PER_100G)
                .fiberPer100g(DEFAULT_FIBER_PER_100G)
                .edibleRatio(DEFAULT_EDIBLE_RATIO)
                .units(Set.of(
                        new UnitResponse("tbsp", 15.0),
                        new UnitResponse("cup", 235.0)))
                .prices(Set.of(
                        new PriceResponse("Masoutis", 6.80, 200),
                        new PriceResponse("MyMarket", 5.70, 175)));
    }

    /**
     * Method for building {@link ListedFoodResponse} DTOs.
     * @return a ListedFoodResponse builder with default values for all fields.
     */
    static ListedFoodResponse.ListedFoodResponseBuilder defaultListedResponseBuilder() {
        return ListedFoodResponse.builder()
                .id(DEFAULT_ID)
                .name(DEFAULT_NAME)
                .brand(DEFAULT_BRAND)
                .caloriesPer100g(DEFAULT_CALORIES_PER_100G)
                .proteinPer100g(DEFAULT_PROTEIN_PER_100G)
                .carbsPer100g(DEFAULT_CARBS_PER_100G)
                .fatPer100g(DEFAULT_FAT_PER_100G)
                .fiberPer100g(DEFAULT_FIBER_PER_100G);
    }

}
