package org.example.mealplannerapp.entity.entry;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.mealplannerapp.constants.Category;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor
public abstract class Entry {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    // day_id

    @Column(nullable = false, length = 15) @Enumerated(EnumType.STRING)
    private Category category;

    // position
    @Column(nullable = false)
    private int position;

    // calories
    @Column(nullable = false) protected double calories;
    @Column(nullable = false) protected double protein;
    @Column(nullable = false) protected double carbs;
    @Column(nullable = false) protected double fat;
    @Column(nullable = false) protected double fiber;
    @Column(nullable = false) protected double price;

    /**
     * <p>Calculates the entry's nutritional values and price, and stores them in the
     * corresponding fields.
     * </p>
     */
    public abstract void snapshotNutritionAndPriceInfo();
}
