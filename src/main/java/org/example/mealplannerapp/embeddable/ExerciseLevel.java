package org.example.mealplannerapp.embeddable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An embeddable class that represents the intensity level of a given
 * exercise and the calories burned per minute at that level.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseLevel {

    /**
     * Name or description of the intensity level.
     */
    private String name;

    /**
     * Calories burned per minute.
     */
    private double caloriesPerMinute;

}
