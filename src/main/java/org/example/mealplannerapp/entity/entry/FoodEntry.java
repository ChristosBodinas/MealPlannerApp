package org.example.mealplannerapp.entity.entry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;

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
     * Food referenced by this entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    /**
     * Quantity of the given food in grams.
     */
    @Column(nullable = false)
    private double grams;

    /**
     * Name of unit last used as a reference.
     */
    @Column(nullable = false, length = 20)
    private String displayUnit;

    /**
     * Name of vendor selected for price calculation.
     */
    @Column(nullable = false, length = 20)
    private String selectedVendor;

    @Override
    public void snapshotNutritionAndPriceInfo() {
        setCalories(food.getCaloriesPer100g() * grams / 100.0);
        setProtein(food.getProteinPer100g() * grams / 100.0);
        setCarbs(food.getCarbsPer100g() * grams / 100.0);
        setFat(food.getFatPer100g() * grams / 100.0);
        setFiber(food.getFiberPer100g() * grams / 100.0);

        Map<String, Double> pricesPer100g = food.derivePricesPer100g();
        if (pricesPer100g.containsKey(selectedVendor)) {
            setPrice(pricesPer100g.get(selectedVendor) * grams / 100);
        } else {
            setPrice(0.0);
        }
    }

    @Override
    public FoodEntry createDuplicate() {
        FoodEntry copy = new FoodEntry();

        copy.setFood(food);
        copy.setGrams(grams);
        copy.setDisplayUnit(displayUnit);
        copy.setSelectedVendor(selectedVendor);
        copy.snapshotNutritionAndPriceInfo();

        return copy;
    }
}