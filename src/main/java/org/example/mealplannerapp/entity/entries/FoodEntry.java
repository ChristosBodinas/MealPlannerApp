package org.example.mealplannerapp.entity.entries;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.mealplannerapp.entity.Food;

@Entity
@Getter @Setter @NoArgsConstructor
public class FoodEntry extends Entry {

    @ManyToOne @JoinColumn(name = "food_id")
    private Food food;

    @Column(nullable = false)
    private double grams;

    // The following fields "remember" which unit & seller the user had last used.
    @Column(nullable = false)
    private String displayUnit;

    @Column(nullable = false)
    private String displaySeller;
}
