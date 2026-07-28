package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.dto.entry.request.EntryDuplicateRequest;
import org.example.mealplannerapp.dto.entry.request.EntryMoveRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.entity.entry.FoodEntry;

public class EntryTestFixtures {

    public static final Category DEFAULT_CATEGORY = Category.BREAKFAST;
    public static final int DEFAULT_POSITION = 1;

    public static final double DEFAULT_GRAMS = 100.0;
    public static final String DEFAULT_DISPLAY_UNIT = "tbsp";
    public static final String DEFAULT_SELECTED_VENDOR = "Masoutis";

    /**
     * Method for building {@link FoodEntry} entities.
     *
     * @return a FoodEntry builder with all fields except {@code id}, {@code day}, and {@code food} filled out
     */
    public static FoodEntry.FoodEntryBuilder<?, ?> defaultFoodEntryBuilder() {
        return FoodEntry.builder()
                .category(DEFAULT_CATEGORY)
                .position(DEFAULT_POSITION)
                .grams(DEFAULT_GRAMS)
                .displayUnit(DEFAULT_DISPLAY_UNIT)
                .selectedVendor(DEFAULT_SELECTED_VENDOR);
    }

    /**
     * Method for building {@link FoodEntryCreateRequest} DTOs.
     *
     * @return a FoodEntryCreateRequest builder with all fields except {@code foodId} filled out
     */
    public static FoodEntryCreateRequest.FoodEntryCreateRequestBuilder defaultFoodEntryCreateRequestBuilder() {
        return FoodEntryCreateRequest.builder()
                .category(DEFAULT_CATEGORY)
                .grams(DEFAULT_GRAMS)
                .displayUnit(DEFAULT_DISPLAY_UNIT)
                .selectedVendor(DEFAULT_SELECTED_VENDOR);
    }

    /**
     * Method for building {@link FoodEntryResponse} DTOs.
     *
     * @return a FoodEntryResponse builder with all fields except {@code id} and {@code foodResponse} filled out
     */
    public static FoodEntryResponse.FoodEntryResponseBuilder defaultFoodEntryResponseBuilder() {
        return FoodEntryResponse.builder()
                .category(DEFAULT_CATEGORY)
                .position(DEFAULT_POSITION)
                .grams(DEFAULT_GRAMS)
                .displayUnit(DEFAULT_DISPLAY_UNIT)
                .selectedVendor(DEFAULT_SELECTED_VENDOR);
    }

    /**
     * Method for building {@link EntryMoveRequest} DTOs.
     * @return an EntryMoveRequest builder with both fields filled out
     */
    public static EntryMoveRequest.EntryMoveRequestBuilder defaultEntryMoveRequestBuilder() {
        return EntryMoveRequest.builder()
            .category(DEFAULT_CATEGORY)
            .desiredPosition(DEFAULT_POSITION);
    }

    /**
     * Method for building {@link EntryDuplicateRequest} DTOs.
     * @return an EntryDuplicateRequest builder with {@code category} filled out and {@code entryId} unset
     */
    public static EntryDuplicateRequest.EntryDuplicateRequestBuilder defaultEntryDuplicateRequestBuilder() {
        return EntryDuplicateRequest.builder()
            .category(DEFAULT_CATEGORY);
    }
}
