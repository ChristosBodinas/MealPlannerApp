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
public class FoodUnit {

    /**
     * Reference unit's name or description.
     */
    private String name;

    /**
     * Reference unit's equivalent in grams.
     */
    private BigDecimal grams;
    
}
