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
public class ReferenceUnit {

    /**
     * Reference unit's name or description.
     */
    private String name;

    /**
     * Reference unit's equivalent in grams.
     */
    private BigDecimal grams;

}
