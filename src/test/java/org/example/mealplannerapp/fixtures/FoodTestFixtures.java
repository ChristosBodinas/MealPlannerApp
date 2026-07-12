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

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class FoodTestFixtures {

    //<editor-fold desc="BUILDERS">
    static FoodRequest.FoodRequestBuilder defaultRequestBuilder() {
        return FoodRequest.builder()
                .name("Fake Food")
                .brand("Fake Brand")
                .caloriesPer100g(97.0)
                .proteinPer100g(12.0).carbsPer100g(37.5).fatPer100g(4.5).fiberPer100g(6.0)
                .edibleRatio(0.9)
                .units(Set.of(
                        new UnitRequest("tbsp", 15.0),
                        new UnitRequest("cup", 235.0)))
                .prices(Set.of(
                        new PriceRequest("Masoutis", 6.80, 200),
                        new PriceRequest("MyMarket", 5.70, 175)));
    }

    static FoodResponse.FoodResponseBuilder defaultResponseBuilder() {
        return FoodResponse.builder()
                .id(99L)
                .name("Fake Food")
                .brand("Fake Brand")
                .caloriesPer100g(97.0)
                .proteinPer100g(12.0).carbsPer100g(37.5).fatPer100g(4.5).fiberPer100g(6.0)
                .edibleRatio(0.9)
                .units(Set.of(
                        new UnitResponse("tbsp", 15.0),
                        new UnitResponse("cup", 235.0)))
                .prices(Set.of(
                        new PriceResponse("Masoutis", 6.80, 200),
                        new PriceResponse("MyMarket", 5.70, 175)));
    }

    static ListedFoodResponse.ListedFoodResponseBuilder defaultListedResponseBuilder() {
        return ListedFoodResponse.builder()
                .id(99L)
                .name("Fake Food")
                .brand("Fake Brand")
                .caloriesPer100g(97.0)
                .proteinPer100g(12.0).carbsPer100g(37.5).fatPer100g(4.5).fiberPer100g(6.0);
    }
    //</editor-fold>

    public static Food defaultFood() {
        Food food = new Food();
        food.setName("Fake Food");
        food.setBrand("Fake Brand");
        food.setCaloriesPer100g(97.0);
        food.setProteinPer100g(12.0);
        food.setCarbsPer100g(37.5);
        food.setFatPer100g(4.5);
        food.setFiberPer100g(6.0);
        food.setEdibleRatio(0.9);
        food.setUnits(Set.of(
                new FoodUnit("tbsp", 15.0),
                new FoodUnit("cup", 235.0)));
        food.setPrices(Set.of(
                new FoodPrice("Masoutis", 6.80, 200),
                new FoodPrice("MyMarket", 5.70, 175)));
        return food;
    }

    public static FoodRequest defaultFoodRequest() {
        return defaultRequestBuilder().build();
    }

    public static FoodRequest duplicateUnitsRequest() {
        return defaultRequestBuilder()
                .units(Set.of(
                        new UnitRequest("tbsp", 15.0),
                        new UnitRequest("tbsp", 235.0)))
                .build();
    }

    public static FoodRequest duplicatePricesRequest() {
        return defaultRequestBuilder()
                .prices(Set.of(
                        new PriceRequest("Masoutis", 6.80, 200),
                        new PriceRequest("Masoutis", 5.70, 175)))
                .build();
    }

    public static FoodResponse defaultFoodResponse() {
        return defaultResponseBuilder().build();
    }

    public static List<ListedFoodResponse> listedResponseList(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> defaultListedResponseBuilder()
                        .id((long) i)
                        .name("Fake Food" + i)
                        .build())
                .toList();
    }
}
