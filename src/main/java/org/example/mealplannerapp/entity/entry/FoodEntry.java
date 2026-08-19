package org.example.mealplannerapp.entity.entry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * An {@link Entry }entity that represents a particular quantity of a single {@link Food}
 * logged in a particular {@link Day}.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FoodEntry extends Entry {

    /**
     * Food referenced by the entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    /**
     * Quantity of the given food in grams.
     */
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal grams;

    /**
     * Name of unit last used as a reference.
     * If null or invalid, the entry's quantity should be displayed in grams.
     */
    @Column(name = "unit_name", nullable = true, length = 10)
    private String unitName;    // TODO: Double check.

    /**
     * Name of vendor selected for price calculation.
     * If null or invalid, the entry's price snapshot will be set to 0.
     */
    @Column(name = "vendor_name", nullable = false, length = 10)
    private String vendorName;  // TODO: Double check.

    @Override
    public void snapshotNutritionAndPriceInfo() {
        setCalories(food.getCalories100g().multiply(grams)
            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP));
        setProtein(food.getProtein100g().multiply(grams)
            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP));
        setCarbs(food.getCarbs100g().multiply(grams)
            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP));
        setFat(food.getFat100g().multiply(grams)
            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP));
        setFiber(food.getFiber100g().multiply(grams)
            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP));

        BigDecimal price100g = food.computePrices100g().entrySet().stream()
            .filter(p -> p.getKey() == vendorName)
            .findFirst()
            .map(p -> p.getValue())
            .orElse(BigDecimal.ZERO);

        setPrice(price100g.multiply(grams)
            .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP));        
    }

    @Override
    public FoodEntry createDuplicate() {
        FoodEntry copy = new FoodEntry();

        copy.setFood(food);
        copy.setGrams(grams);
        copy.setUnitName(unitName);
        copy.setVendorName(vendorName);
        copy.snapshotNutritionAndPriceInfo();

        return copy;
    }
}