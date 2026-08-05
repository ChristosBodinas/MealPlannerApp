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
public class ExerciseIntensity {

    private String name; // Name/description of intensity level.

    private double caloriesPerMinute; // Calories burned per minute.

}