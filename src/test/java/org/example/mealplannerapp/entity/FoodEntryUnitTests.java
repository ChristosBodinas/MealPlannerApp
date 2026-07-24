package org.example.mealplannerapp.entity;

import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.*;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.*;

public class FoodEntryUnitTests {

    private FoodEntry entry;
    private Food food;

    private static final double GRAMS = 150.0;
    private static final String DISPLAY_UNIT = "cup";
    private static final String VALID_VENDOR = "MyMarket";
    private static final String INVALID_VENDOR = "Masoutis";

    private static final double CALORIES_PER_100G = 97.0;
    private static final double PROTEIN_PER_100G = 12.0;
    private static final double CARBS_PER_100G = 37.5;
    private static final double FAT_PER_100G = 4.5;
    private static final double FIBER_PER_100G = 6.0;
    private static final double EDIBLE_RATIO = 1.0;
    private static final double PURCHASE_PRICE = 7.00;
    private static final double PURCHASE_GRAMS = 100.0;

    @BeforeEach
    void prepareAllTests() {

    }

    @Nested
    class snapshotNutritionAndPriceInfo {

        @BeforeEach
        void prepareTests() {
            food = defaultFoodBuilder()
                    .caloriesPer100g(CALORIES_PER_100G)
                    .proteinPer100g(PROTEIN_PER_100G)
                    .carbsPer100g(CARBS_PER_100G)
                    .fatPer100g(FAT_PER_100G)
                    .fiberPer100g(FIBER_PER_100G)
                    .edibleRatio(EDIBLE_RATIO)
                    .build();

            entry = defaultFoodEntryBuilder().grams(GRAMS).build();
        }

        @Test
        void vendorExists() {
            // Arrange

            // Act

            // Assert
        }

        @Test
        void vendorNotFound() {

        }
    }

    @Nested
    class createDuplicate {

        private final double SOURCE_CALORIES_PER_100G = 50.0;
        private final double SOURCE_GRAMS = 120.0;
        private final String SOURCE_UNIT = "tbsp";
        private final String SOURCE_VENDOR = "Sklavenitis";

        @Test
        void duplicateCreated() {
            // Arrange
            food = defaultFoodBuilder()
                    .caloriesPer100g(CALORIES_PER_100G)
                    .build();

            entry = defaultFoodEntryBuilder()
                    .food(food)
                    .grams(SOURCE_GRAMS)
                    .displayUnit(SOURCE_UNIT)
                    .selectedVendor(SOURCE_VENDOR)
                    .build();

            // Act
            FoodEntry copy = entry.createDuplicate();

            // Assert
            assertThat(copy).isNotSameAs(entry);
            assertThat(copy.getFood()).isEqualTo(entry.getFood());
            assertThat(copy.getGrams()).isEqualTo(entry.getGrams());
            assertThat(copy.getDisplayUnit()).isEqualTo(entry.getDisplayUnit());
            assertThat(copy.getSelectedVendor()).isEqualTo(entry.getSelectedVendor());


        }

    }
}
