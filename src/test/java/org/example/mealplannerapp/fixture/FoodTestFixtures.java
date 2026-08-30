package org.example.mealplannerapp.fixture;

import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.UnitRequest;
import org.example.mealplannerapp.dto.food.request.VendorRequest;
import org.example.mealplannerapp.embeddable.ReferenceUnit;
import org.example.mealplannerapp.embeddable.VendorData;
import org.example.mealplannerapp.entity.Food;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class FoodTestFixtures {

    private static final String DEFAULT_NAME = "Chicken Breast";
    private static final String DEFAULT_BRAND = "Pindos";
    private static final BigDecimal DEFAULT_CALORIES_100G = new BigDecimal("120.0");
    private static final BigDecimal DEFAULT_PROTEIN_100G = new BigDecimal("30.0");
    private static final BigDecimal DEFAULT_CARBS_100G = new BigDecimal("50.0");
    private static final BigDecimal DEFAULT_FAT_100G = new BigDecimal("10.0");
    private static final BigDecimal DEFAULT_FIBER_100G = new BigDecimal("5.0");
    private static final BigDecimal DEFAULT_EDIBLE_RATIO = new BigDecimal("0.9");

    private static final String DEFAULT_UNIT_NAME_1 = "tbsp";
    private static final BigDecimal DEFAULT_UNIT_GRAMS_1 = new BigDecimal("15.0");
    private static final String DEFAULT_UNIT_NAME_2 = "cup";
    private static final BigDecimal DEFAULT_UNIT_GRAMS_2 = new BigDecimal("20.0");

    private static final String DEFAULT_VENDOR_NAME_1 = "Masoutis";
    private static final BigDecimal DEFAULT_PURCHASE_PRICE_1 = new BigDecimal("50.0");
    private static final BigDecimal DEFAULT_PURCHASE_GRAMS_1 = new BigDecimal("200.0");
    private static final String DEFAULT_VENDOR_NAME_2 = "MyMarket";
    private static final BigDecimal DEFAULT_PURCHASE_PRICE_2 = new BigDecimal("40.0");
    private static final BigDecimal DEFAULT_PURCHASE_GRAMS_2 = new BigDecimal("150.0");

    /**
     * Builds a {@link Food} entity fixture for testing.
     *
     * @return a Food builder with null {@code id} and {@code user},
     * and default values in all other fields
     */
    public static Food.FoodBuilder defaultFood() {
        Set<ReferenceUnit> defaultUnits = new HashSet<>(Set.of(
                new ReferenceUnit(DEFAULT_UNIT_NAME_1, DEFAULT_UNIT_GRAMS_1),
                new ReferenceUnit(DEFAULT_UNIT_NAME_2, DEFAULT_UNIT_GRAMS_2)));

        Set<VendorData> defaultVendors = new HashSet<>(Set.of(
                new VendorData(DEFAULT_VENDOR_NAME_1, DEFAULT_PURCHASE_PRICE_1,
                        DEFAULT_PURCHASE_GRAMS_1),
                new VendorData(DEFAULT_VENDOR_NAME_2, DEFAULT_PURCHASE_PRICE_2,
                        DEFAULT_PURCHASE_GRAMS_2)));

        return Food.builder()
                .name(DEFAULT_NAME)
                .brand(DEFAULT_BRAND)
                .calories100g(DEFAULT_CALORIES_100G)
                .protein100g(DEFAULT_PROTEIN_100G)
                .carbs100g(DEFAULT_CARBS_100G)
                .fat100g(DEFAULT_FAT_100G)
                .fiber100g(DEFAULT_FIBER_100G)
                .edibleRatio(DEFAULT_EDIBLE_RATIO)
                .units(defaultUnits)
                .vendors(defaultVendors);
    }

    public static FoodRequest.FoodRequestBuilder defaultFoodRequest() {
        Set<UnitRequest> defaultUnits = new HashSet<>(Set.of(
                new UnitRequest(DEFAULT_UNIT_NAME_1, DEFAULT_UNIT_GRAMS_1),
                new UnitRequest(DEFAULT_UNIT_NAME_2, DEFAULT_UNIT_GRAMS_2)));

        Set<VendorRequest> defaultVendors = new HashSet<>(Set.of(
                new VendorRequest(DEFAULT_VENDOR_NAME_1, DEFAULT_PURCHASE_PRICE_1,
                        DEFAULT_PURCHASE_GRAMS_1),
                new VendorRequest(DEFAULT_VENDOR_NAME_2, DEFAULT_PURCHASE_PRICE_2,
                        DEFAULT_PURCHASE_GRAMS_2)));

        return FoodRequest.builder()
                .name(DEFAULT_NAME)
                .brand(DEFAULT_BRAND)
                .calories100g(DEFAULT_CALORIES_100G)
                .protein100g(DEFAULT_PROTEIN_100G)
                .carbs100g(DEFAULT_CARBS_100G)
                .fat100g(DEFAULT_FAT_100G)
                .fiber100g(DEFAULT_FIBER_100G)
                .edibleRatio(DEFAULT_EDIBLE_RATIO)
                .units(defaultUnits)
                .vendors(defaultVendors);
    }
}
