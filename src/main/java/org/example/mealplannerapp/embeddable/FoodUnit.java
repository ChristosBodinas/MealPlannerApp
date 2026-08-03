package org.example.mealplannerapp.embeddable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An embeddable class that represents a reference unit for a food
 * and its equivalent amount in grams.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodUnit {

    /**
     * Name of the reference unit.
     */
    private String name;

    /**
     * Quantity expressed in grams.
     */
    private double grams;
}