package org.example.mealplannerapp.entity;

import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.*;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.*;

import java.util.Set;

import org.example.mealplannerapp.embeddable.FoodPrice;

public class FoodEntryUnitTests {

    private FoodEntry entry;
    private Food food;

    @Nested
    class snapshotNutritionAndPriceInfo {

        private final double GRAMS = 150.0;
        
        private final String VALID_VENDOR = "MyMarket";
        private final String INVALID_VENDOR = "Masoutis";

        private final double CALORIES_PER_100G = 97.0;
        private final double PROTEIN_PER_100G = 12.0;
        private final double CARBS_PER_100G = 37.5;
        private final double FAT_PER_100G = 4.5;
        private final double FIBER_PER_100G = 6.0;

        private final double EDIBLE_RATIO = 1.0;
        private final double PURCHASE_PRICE = 7.00;
        private final double PURCHASE_GRAMS = 100.0;

        private final double TARGET_CALORIES = CALORIES_PER_100G * GRAMS / 100.0;
        private final double TARGET_PROTEIN = PROTEIN_PER_100G * GRAMS / 100.0;
        private final double TARGET_CARBS = CARBS_PER_100G * GRAMS / 100.0;
        private final double TARGET_FAT = FAT_PER_100G * GRAMS / 100.0;
        private final double TARGET_FIBER = FIBER_PER_100G * GRAMS / 100.0;
        private final double TARGET_PRICE = GRAMS * PURCHASE_PRICE / (PURCHASE_GRAMS * EDIBLE_RATIO);

        @BeforeEach
        void prepareTests() {
            food = defaultFoodBuilder()
                    .caloriesPer100g(CALORIES_PER_100G)
                    .proteinPer100g(PROTEIN_PER_100G)
                    .carbsPer100g(CARBS_PER_100G)
                    .fatPer100g(FAT_PER_100G)
                    .fiberPer100g(FIBER_PER_100G)
                    .edibleRatio(EDIBLE_RATIO)
                    .prices(Set.of(new FoodPrice(VALID_VENDOR, PURCHASE_PRICE, PURCHASE_GRAMS)))
                    .build();

            entry = defaultFoodEntryBuilder().food(food).grams(GRAMS).build();
        }

        @Test
        @DisplayName("Correctly calculates nutrition value snapshots.")
        void nutritionSnapshots() {
            // Act
            entry.snapshotNutritionAndPriceInfo();

            // Assert
            assertThat(entry.getCalories()).isCloseTo(TARGET_CALORIES, within(0.01));
            assertThat(entry.getProtein()).isCloseTo(TARGET_PROTEIN, within(0.01));
            assertThat(entry.getCarbs()).isCloseTo(TARGET_CARBS, within(0.01));
            assertThat(entry.getFat()).isCloseTo(TARGET_FAT, within(0.01));
            assertThat(entry.getFiber()).isCloseTo(TARGET_FIBER, within(0.01));
        }

        @Test
        @DisplayName("When the selected vendor exists in the food's pricing data, calculates the price.")
        void priceSnapshotVendorExists() {
            // Arrange
            entry.setSelectedVendor(VALID_VENDOR);

            // Act
            entry.snapshotNutritionAndPriceInfo();

            // Assert
            assertThat(entry.getPrice()).isCloseTo(TARGET_PRICE, within(0.01));
        }

        @Test
        @DisplayName("When the selected vendor does not exist in the food's pricing data, sets price to 0.")
        void priceSnapshotVendorNotFound() {
            // Arrange
            entry.setSelectedVendor(INVALID_VENDOR);

            // Act
            entry.snapshotNutritionAndPriceInfo();

            // Assert
            assertThat(entry.getPrice()).isCloseTo(0.00, within(0.01));
        }

    }

    @Nested
    class createDuplicate {

        private final double SOURCE_CALORIES_PER_100G = 50.0;
        private final double SOURCE_GRAMS = 120.0;
        private final double TARGET_CALORIES = SOURCE_CALORIES_PER_100G * SOURCE_GRAMS / 100.0;

        private final String SOURCE_UNIT = "tbsp";
        private final String SOURCE_VENDOR = "Sklavenitis";

        @Test
        @DisplayName("Correctly outputs a new, distinct FoodEntry and independently calculates snapshot values.")
        void duplicateCreated() {
            // Arrange
            food = defaultFoodBuilder()
                    .caloriesPer100g(SOURCE_CALORIES_PER_100G)
                    .build();

            entry = defaultFoodEntryBuilder()
                    .food(food)
                    .calories(0.0)
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
            
            // NOTE: entry's calories have manually been set to 0.00, so the following assert
            // also tests that copy has properly calculated its own snapshot values and not
            // merely duplicated them from the original.
            assertThat(copy.getCalories()).isCloseTo(TARGET_CALORIES, within(0.01));
        }

    }
}
