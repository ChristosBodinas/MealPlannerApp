package org.example.mealplannerapp.embeddable;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseLevel {

    /**
     * Intensity level description.
     */
    private String intensityDesc;

    /**
     * Calories (in Kcal) burned per minute at the particular intensity level.
     */
    private BigDecimal caloriesPerMinute;
    
}
