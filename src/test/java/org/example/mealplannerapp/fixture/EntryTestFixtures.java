package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.constants.Category;
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
     * @return a FoodEntry builder with {@code category}, {@code position}, {@code grams},
     * {@code displayUnit}, and {@code selectedVendor} filled out
     */
    public static FoodEntry.FoodEntryBuilder<?, ?> defaultFoodEntryBuilder() {
        return FoodEntry.builder()
                .category(DEFAULT_CATEGORY)
                .position(DEFAULT_POSITION)
                .grams(DEFAULT_GRAMS)
                .displayUnit(DEFAULT_DISPLAY_UNIT)
                .selectedVendor(DEFAULT_SELECTED_VENDOR);
    }
}
