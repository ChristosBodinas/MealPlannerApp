package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.common.Category;
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
                .unit(DEFAULT_DISPLAY_UNIT)
                .vendor(DEFAULT_SELECTED_VENDOR);
    }

}