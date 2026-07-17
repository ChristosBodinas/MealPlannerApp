package org.example.mealplannerapp.embeddable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FoodPrice {

    // The name of the merchant selling the food.
    private String merchant;

    // The amount of money spent on buying the product.
    private double purchasePrice;

    // The amount (in grams) of product (including non-edible parts) bought.
    private double purchaseGrams;

}
