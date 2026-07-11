package org.example.mealplannerapp.service;

import java.util.Set;

import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.dto.entry.request.EntryBulkRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.FoodEntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.springframework.test.util.ReflectionTestUtils;

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

    static FoodEntryEditRequest.FoodEntryEditRequestBuilder defaultFoodEntryEditRequestBuilder() {
        return FoodEntryEditRequest.builder()
                .grams(2.00)
                .displayUnit("tsp")
                .displayMerchant("MyMarket");
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

    static EntryBulkRequest defaultEntryBulkRequest() {
        return new EntryBulkRequest(Set.of(5L, 10L, 15L, 20L, 25L));
    }

    static FoodEntry defaultFoodEntry() {
        FoodEntry entry = new FoodEntry();
        entry.setCategory(Category.BREAKFAST);
        entry.setPosition(1);
        entry.setGrams(100.0);
        entry.setDisplayUnit("tbsp");
        entry.setDisplayMerchant("Masoutis");
        return entry;
    }

    static FoodEntry foodEntryWithId(Long id) {
        FoodEntry entry = new FoodEntry();
        ReflectionTestUtils.setField(entry, "id", entry);
        return entry;
    }

    static FoodEntryCreateRequest defaultFoodEntryCreateRequest() {
        return defaultFoodEntryCreateRequestBuilder().build();
    }

    static FoodEntryEditRequest defaultFoodEntryEditRequest() {
        return defaultFoodEntryEditRequestBuilder().build();
    }

    static FoodEntryResponse defaultFoodEntryResponse(FoodResponse foodResponse) {
        return defaultFoodEntryResponseBuilder()
                .foodResponse(foodResponse)
                .build();
    }
}
