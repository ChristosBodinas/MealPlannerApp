package org.example.mealplannerapp.entity.entry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.mealplannerapp.embeddable.ReferenceUnit;
import org.example.mealplannerapp.embeddable.VendorData;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * An {@link Entry} that represents a particular quantity of a given {@link Food}
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
    @JoinColumn(name = "food_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Food food;

    /**
     * Quantity of the referenced food in grams.
     */
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal grams;

    /**
     * Quantity of the referenced food in the selected unit.
     * Set to null if {@code unitName} is null.
     */
    @Column(name = "unit_quantity", precision = 6, scale = 2)
    private BigDecimal unitQuantity;

    /**
     * Name of the reference unit selected for display.
     * If null, the entry's quantity should be displayed in grams.
     */
    @Column(name = "unit_name", length = 10)
    private String unitName;

    /**
     * Name of the vendor selected for price calculation.
     * If null, the entry's price will be set to 0.
     */
    @Column(name = "vendor_name", length = 10)
    private String vendorName;


    @Override
    public void snapshotInfo() {
        // Snapshot entry name.
        if (food.getBrand() == null) {
            setName(food.getName());
        } else {
            setName(food.getBrand() + ", " + food.getName());
        }

        BigDecimal hundred = BigDecimal.valueOf(100);

        // Snapshot nutrition values.
        setCalories(food.getCalories100g().multiply(grams).divide(hundred, RoundingMode.HALF_UP));
        setProtein(food.getProtein100g().multiply(grams).divide(hundred, RoundingMode.HALF_UP));
        setCarbs(food.getCarbs100g().multiply(grams).divide(hundred, RoundingMode.HALF_UP));
        setFat(food.getFat100g().multiply(grams).divide(hundred, RoundingMode.HALF_UP));
        setFiber(food.getFiber100g().multiply(grams).divide(hundred, RoundingMode.HALF_UP));

        // Snapshot price.
        BigDecimal price100g = food.computePrices100g().entrySet().stream()
                .filter(p -> p.getKey().equals(vendorName))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(BigDecimal.ZERO);

        setPrice(price100g.multiply(grams).divide(hundred, RoundingMode.HALF_UP));
    }

    @Override
    public FoodEntry createDuplicate() {
        FoodEntry copy = new FoodEntry();

        copy.setFood(food);
        copy.setGrams(grams);

        boolean unitStillValid = food.getUnits().stream()
                .map(ReferenceUnit::getName)
                .anyMatch(name -> name.equals(unitName));

        if (unitStillValid) {
            copy.setUnitName(unitName);
            copy.setUnitQuantity(unitQuantity);
        } else {
            copy.setUnitName(null);
            copy.setUnitQuantity(null);
        }

        boolean vendorStillValid = food.getVendors().stream()
                .map(VendorData::getName)   // TODO: Are these unnecessary?
                .anyMatch(name -> name.equals(vendorName));

        if (vendorStillValid) {
            copy.setVendorName(vendorName);
        } else {
            copy.setVendorName(null);   // TODO: Can be shortened.
        }

        copy.snapshotInfo();
        return copy;
    }

}
