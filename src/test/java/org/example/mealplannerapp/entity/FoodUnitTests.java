package org.example.mealplannerapp.entity;

import org.example.mealplannerapp.embeddable.FoodPrice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFoodBuilder;

public class FoodUnitTests {

    private Food food;

    @Nested
    class derivePricesPer100g {

        @Test
        @DisplayName("If pricing data exists, correctly calculates the prices per 100g.")
        void pricesDerivedFromNonEmpty() {
            // Arrange
            final String VENDOR = "Masoutis";
            final double PURCHASE_PRICE = 7.00;
            final double PURCHASE_GRAMS = 200;
            final double EDIBLE_RATIO = 0.6;
            final double PRICE_PER_100G = 5.83;

            FoodPrice price = new FoodPrice(VENDOR, PURCHASE_PRICE, PURCHASE_GRAMS);
            food = defaultFoodBuilder().edibleRatio(EDIBLE_RATIO).prices(Set.of(price)).build();

            // Act
            Map<String, Double> results = food.derivePricesPer100g();

            // Assert
            assertThat(results).containsOnlyKeys(VENDOR);
            assertThat(results.get(VENDOR)).isCloseTo(PRICE_PER_100G, within(0.01));
        }

        @Test
        @DisplayName("If no pricing data exists, returns an empty map.")
        void pricesAreEmpty() {
            // Arrange
            food = defaultFoodBuilder().prices(Set.of()).build();

            // Act
            Map<String, Double> results = food.derivePricesPer100g();

            // Assert
            assertThat(results).isEmpty();
        }

    }

}
