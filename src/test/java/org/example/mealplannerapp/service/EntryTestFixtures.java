package org.example.mealplannerapp.service;

import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.dto.entry.request.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.entry.FoodEntry;

public class EntryTestFixtures {

    //<editor-fold desc="BUILDERS">
    static FoodEntryCreateRequest.FoodEntryCreateRequestBuilder defaultFoodEntryCreateRequestBuilder() {
        return FoodEntryCreateRequest.builder()
                .category(Category.BREAKFAST)
                .position(1)
                .foodId(99L)
                .grams(100.0)
                .displayUnit("tbsp")
                .displayMerchant("Masoutis");
    }

    static FoodEntryResponse.FoodEntryResponseBuilder defaultFoodEntryResponseBuilder() {
        return FoodEntryResponse.builder()
                .id(77L)
                .category(Category.BREAKFAST)
                .position(1)
                .grams(100.0)
                .displayUnit("tbsp")
                .displayMerchant("Masoutis");
    }
    //</editor-fold>

    static FoodEntry defaultFoodEntry() {
        FoodEntry entry = new FoodEntry();
        entry.setCategory(Category.BREAKFAST);
        entry.setPosition(1);
        entry.setGrams(100.0);
        entry.setDisplayUnit("tbsp");
        entry.setDisplayMerchant("Masoutis");
        return entry;
    }

    static FoodEntryCreateRequest defaultFoodEntryCreateRequest() {
        return defaultFoodEntryCreateRequestBuilder().build();
    }

    static FoodEntryResponse defaultFoodEntryResponse(FoodResponse foodResponse) {
        return defaultFoodEntryResponseBuilder()
                .foodResponse(foodResponse)
                .build();
    }
}
