package org.example.mealplannerapp.fixture;

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

import java.util.HashSet;
import java.util.Set;

public class FoodTestFixtures {

    private static final String DEFAULT_NAME = "Chicken Breast";
    private static final String DEFAULT_BRAND = "Pindos";
    private static final double DEFAULT_CALORIES_PER_100G = 97.0;
    private static final double DEFAULT_PROTEIN_PER_100G = 12.0;
    private static final double DEFAULT_CARBS_PER_100G = 37.5;
    private static final double DEFAULT_FAT_PER_100G = 4.5;
    private static final double DEFAULT_FIBER_PER_100G = 6.0;
    private static final double DEFAULT_EDIBLE_RATIO = 1.0;

    /**
     * Method for building {@link Food} entities.
     *
     * @return a Food builder with all fields except {@code id} and {@code user} filled out
     */
    public static Food.FoodBuilder defaultFoodBuilder() {
        return Food.builder()
                .name(DEFAULT_NAME)
                .brand(DEFAULT_BRAND)
                .caloriesPer100g(DEFAULT_CALORIES_PER_100G)
                .proteinPer100g(DEFAULT_PROTEIN_PER_100G)
                .carbsPer100g(DEFAULT_CARBS_PER_100G)
                .fatPer100g(DEFAULT_FAT_PER_100G)
                .fiberPer100g(DEFAULT_FIBER_PER_100G)
                .edibleRatio(DEFAULT_EDIBLE_RATIO)
                .units(new HashSet<>(Set.of(
                        new FoodUnit("tbsp", 15.0),
                        new FoodUnit("cup", 235.0))))
                .prices(new HashSet<>(Set.of(
                        new FoodPrice("Masoutis", 6.80, 200),
                        new FoodPrice("MyMarket", 5.70, 175))));
    }

    /**
     * Method for building {@link FoodRequest} DTOs.
     *
     * @return a FoodRequest builder with all fields filled out
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
                .units(new HashSet<>(Set.of(
                        new UnitRequest("tbsp", 15.0),
                        new UnitRequest("cup", 235.0))))
                .prices(new HashSet<>(Set.of(
                        new PriceRequest("Masoutis", 6.80, 200),
                        new PriceRequest("MyMarket", 5.70, 175))));
    }

    /**
     * Method for building {@link FoodResponse} DTOs.
     *
     * @return a FoodResponse builder with all fields except {@code id} filled out
     */
    public static FoodResponse.FoodResponseBuilder defaultFoodResponseBuilder() {
        return FoodResponse.builder()
                .name(DEFAULT_NAME)
                .brand(DEFAULT_BRAND)
                .caloriesPer100g(DEFAULT_CALORIES_PER_100G)
                .proteinPer100g(DEFAULT_PROTEIN_PER_100G)
                .carbsPer100g(DEFAULT_CARBS_PER_100G)
                .fatPer100g(DEFAULT_FAT_PER_100G)
                .fiberPer100g(DEFAULT_FIBER_PER_100G)
                .edibleRatio(DEFAULT_EDIBLE_RATIO)
                .units(new HashSet<>(Set.of(
                        new UnitResponse("tbsp", 15.0),
                        new UnitResponse("cup", 235.0))))
                .prices(new HashSet<>(Set.of(
                        new PriceResponse("Masoutis", 6.80, 200),
                        new PriceResponse("MyMarket", 5.70, 175))));
    }

    /**
     * Method for building {@link ListedFoodResponse} DTOs.
     *
     * @return a ListedFoodResponse builder with all fields except {@code id} filled out
     */
    public static ListedFoodResponse.ListedFoodResponseBuilder defaultListedFoodResponseBuilder() {
        return ListedFoodResponse.builder()
                .name(DEFAULT_NAME)
                .brand(DEFAULT_BRAND)
                .caloriesPer100g(DEFAULT_CALORIES_PER_100G)
                .proteinPer100g(DEFAULT_PROTEIN_PER_100G)
                .carbsPer100g(DEFAULT_CARBS_PER_100G)
                .fatPer100g(DEFAULT_FAT_PER_100G)
                .fiberPer100g(DEFAULT_FIBER_PER_100G);
    }

}
