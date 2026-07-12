package org.example.mealplannerapp.entity.entry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.mealplannerapp.entity.Food;

import java.util.Map;

@Entity
@Getter @Setter @NoArgsConstructor
public class FoodEntry extends Entry {

    @ManyToOne @JoinColumn(name = "food_id")
    private Food food;

    @Column(nullable = false)
    private double grams;

    @Column(nullable = false, length = 20)
    private String displayUnit;

    @Column(nullable = false, length = 20)
    private String displayMerchant;

    @Override
    public void snapshotNutritionAndPriceInfo() {
        calories = food.getCaloriesPer100g() * grams / 100.0;
        protein = food.getProteinPer100g() * grams / 100.0;
        carbs = food.getCarbsPer100g() * grams / 100.0;
        fat = food.getFatPer100g() * grams / 100.0;
        fiber = food.getFiberPer100g() * grams / 100.0;

        Map<String, Double> pricesPer100g = food.derivePricesPer100g();
        if (!pricesPer100g.isEmpty() && pricesPer100g.containsKey(displayMerchant)) {
            price = pricesPer100g.get(displayMerchant) * grams / 100;
        } else {
            price = 0.00;
        }
    }

    @Override
    public FoodEntry createDuplicate() {
        FoodEntry copy = new FoodEntry();

        copy.setFood(food);
        copy.setGrams(grams);
        copy.setDisplayUnit(displayUnit);
        copy.setDisplayMerchant(displayMerchant);
        copy.snapshotNutritionAndPriceInfo();

        return copy;
    }
}
