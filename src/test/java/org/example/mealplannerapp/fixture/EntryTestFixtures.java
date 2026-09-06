package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;

import java.math.BigDecimal;

public class EntryTestFixtures {

    // TODO: Write fixtures.

    private static final Category DEFAULT_CATEGORY = Category.BREAKFAST;
    private static final int DEFAULT_POSITION = 1;
    private static final String DEFAULT_NAME = "My Entry";

    private static final BigDecimal DEFAULT_GRAMS = BigDecimal.valueOf(100.0);
    private static final String DEFAULT_UNIT_NAME = "tbsp";
    private static final String DEFAULT_VENDOR_NAME = "Masoutis";

    private static final BigDecimal DEFAULT_DURATION = BigDecimal.valueOf(30.0);
    private static final String DEFAULT_LEVEL_NAME = "Moderate";

    public static FoodEntry.FoodEntryBuilder<?, ?> defaultFoodEntry() {
        return FoodEntry.builder()
                .category(DEFAULT_CATEGORY)
                .position(DEFAULT_POSITION)
                .name(DEFAULT_NAME)
                .grams(DEFAULT_GRAMS)
                .unitName(DEFAULT_UNIT_NAME)
                .vendorName(DEFAULT_VENDOR_NAME)
                .calories(BigDecimal.ONE)
                .protein(BigDecimal.ONE)
                .carbs(BigDecimal.ONE)
                .fat(BigDecimal.ONE)
                .fiber(BigDecimal.ONE)
                .price(BigDecimal.ONE);
    }

    public static ExerciseEntry.ExerciseEntryBuilder<?, ?> defaultExerciseEntry() {
        return ExerciseEntry.builder()
                .category(DEFAULT_CATEGORY)
                .position(DEFAULT_POSITION)
                .name(DEFAULT_NAME)
                .duration(DEFAULT_DURATION)
                .levelName(DEFAULT_LEVEL_NAME)
                .calories(BigDecimal.valueOf(-1.0))
                .protein(BigDecimal.ZERO)
                .carbs(BigDecimal.ZERO)
                .fat(BigDecimal.ZERO)
                .fiber(BigDecimal.ZERO)
                .price(BigDecimal.ZERO);
    }
}
