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
public class EffortLevel {

    /**
     * Effort level's name or description.
     */
    private String name;

    /**
     * Amount (in Kcal) of calories burned per minute at this effort level.
     */
    private BigDecimal burnRate;

}
