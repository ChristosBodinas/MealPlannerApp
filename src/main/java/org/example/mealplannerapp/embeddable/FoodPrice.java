package org.example.mealplannerapp.embeddable;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class FoodPrice {

    /**
     * Vendor's name.
     */
    private String vendorName;

    /**
     * Price to purchase a single unit of product.
     */
    private BigDecimal purchasePrice;

    /**
     * Grams in a single unit of product.
     */
    private BigDecimal purchaseGrams;
    
}
