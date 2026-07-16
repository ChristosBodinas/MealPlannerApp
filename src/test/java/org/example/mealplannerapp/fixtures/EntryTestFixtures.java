package org.example.mealplannerapp.fixtures;

import java.util.Set;

import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.entity.entry.FoodEntry;

public class EntryTestFixtures {

    private static final Long DEFAULT_ID = 1L;
    private static final Category DEFAULT_CATEGORY = Category.BREAKFAST;
    private static final int DEFAULT_POSITION = 1;

    private static final double DEFAULT_GRAMS = 150.0;
    private static final String DEFAULT_DISPLAY_UNIT = "tbsp";
    private static final String DEFAULT_DISPLAY_MERCHANT = "Masoutis";

    /**
     * Method for building {@link FoodEntry} entities.
     * @return a FoodEntry builder with default values in all fields except {@code day} and {@code food}
     */
    public static FoodEntry.FoodEntryBuilder<?, ?> defaultFoodEntryBuilder() {
        return FoodEntry.builder()
                .id(DEFAULT_ID)
                .category(DEFAULT_CATEGORY)
                .position(DEFAULT_POSITION)
                .grams(DEFAULT_GRAMS)
                .displayUnit(DEFAULT_DISPLAY_UNIT)
                .displayMerchant(DEFAULT_DISPLAY_MERCHANT);
    }

    /**
     * Method for building {@link FoodEntryCreateRequest} DTOs.
     * @return a FoodEntryCreateRequest builder with default values in all fields except {@code foodId}
     */
    public static FoodEntryCreateRequest.FoodEntryCreateRequestBuilder defaultFoodEntryCreateRequestBuilder() {
        return FoodEntryCreateRequest.builder()
                .category(DEFAULT_CATEGORY)
                .grams(DEFAULT_GRAMS)
                .displayUnit(DEFAULT_DISPLAY_UNIT)
                .displayMerchant(DEFAULT_DISPLAY_MERCHANT);
    }

    /**
     * Method for building {@link FoodEntryResponse} DTOs.
     * @return a FoodEntryResponse builder with default values in all fields except {@code foodResponse}
     */
    public static FoodEntryResponse.FoodEntryResponseBuilder defaultFoodEntryResponseBuilder() {
        return FoodEntryResponse.builder()
                .id(DEFAULT_ID)
                .category(DEFAULT_CATEGORY)
                .position(DEFAULT_POSITION)
                .grams(DEFAULT_GRAMS)
                .displayUnit(DEFAULT_DISPLAY_UNIT)
                .displayMerchant(DEFAULT_DISPLAY_MERCHANT);
    }
}
