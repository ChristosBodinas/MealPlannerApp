package org.example.mealplannerapp.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter @Setter @NoArgsConstructor
public class FoodUnit {

    // The name of the unit.
    private String name;

    // The equivalent amount of grams for the given unit.
    private double grams;
}
