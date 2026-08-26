package org.example.mealplannerapp.embeddable;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorData {

    /**
     * Vendor name.
     */
    private String name;

    /**
     * Cost (in euros) for a single unit of sold product.
     */
    private BigDecimal purchasePrice;

    /**
     * Grams in a single unit of sold product.
     */
    private BigDecimal purchaseGrams;

}
