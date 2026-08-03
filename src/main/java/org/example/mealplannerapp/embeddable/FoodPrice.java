package org.example.mealplannerapp.embeddable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An embeddable class that represents a food's pricing data
 * for a particular vendor.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodPrice {

    /**
     * Name of the vendor selling the food.
     */
    private String vendor;

    /**
     * Purchase price for the given quantity, expressed in euros.
     */
    private double purchasePrice;

    /**
     * Purchase amount expressed in grams.
     */
    private double purchaseGrams;

}