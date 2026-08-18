package org.example.mealplannerapp.embeddable;

import java.math.BigDecimal;

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
