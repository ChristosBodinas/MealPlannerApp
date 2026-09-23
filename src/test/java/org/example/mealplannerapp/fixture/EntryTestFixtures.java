package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;

import java.math.BigDecimal;

public class EntryTestFixtures {

    private static final Category DEFAULT_CATEGORY = Category.BREAKFAST;

    private static final BigDecimal DEFAULT_GRAMS = BigDecimal.valueOf(90.0);
    private static final BigDecimal DEFAULT_UNIT_QUANTITY = BigDecimal.valueOf(6.0);
    private static final String DEFAULT_UNIT = "tbsp";
    private static final String DEFAULT_VENDOR = "Masoutis";

    private static final BigDecimal DEFAULT_DURATION = BigDecimal.valueOf(30.0);
    private static final String DEFAULT_LEVEL = "Slow";

    /**
     * Method for building {@link FoodEntry} fixtures for testing
     * @return a FoodEntry builder with default values in {@code category}, {@code grams},
     * {@code unitQuantity}, {@code unitName}, and {@code vendorName}
     */
    public static FoodEntry.FoodEntryBuilder<?, ?> defaultFoodEntry() {
        return FoodEntry.builder()
                .category(DEFAULT_CATEGORY)
                .grams(DEFAULT_GRAMS)
                .unitQuantity(DEFAULT_UNIT_QUANTITY)
                .unitName(DEFAULT_UNIT)
                .vendorName(DEFAULT_VENDOR);
    }

    /**
     * Method for building {@link ExerciseEntry} fixtures for testing
     * @return an ExerciseEntry builder with default values in {@code category}, {@code duration},
     * and {@code levelName}
     */
    public static ExerciseEntry.ExerciseEntryBuilder<?, ?> defaultExerciseEntry() {
        return ExerciseEntry.builder()
                .category(DEFAULT_CATEGORY)
                .duration(DEFAULT_DURATION)
                .levelName(DEFAULT_LEVEL);
    }
}
