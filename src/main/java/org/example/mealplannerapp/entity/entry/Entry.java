package org.example.mealplannerapp.entity.entry;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.entity.Day;

/**
 * Base class for all entry entities. Implemented via joined inheritance.
 * Includes positioning data and nutrition/price snapshots calculated on
 * entry creation and update.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * Day to which the entry belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private Day day;

    /**
     * Category to which the entry belongs.
     */
    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private Category category;

    /**
     * One-based osition of the entry within its category and scoped to its day.
     */
    @Column(nullable = false)
    private int position;

    /**
     * Energy amount (in Kcal) snapshot.
     */
    @Column(nullable = false)
    private double calories;

    /**
     * Protein amount (in grams) snapshot.
     */
    @Column(nullable = false)
    private double protein;

    /**
     * Carbs amount (in grams) snapshot.
     */
    @Column(nullable = false)
    private double carbs;

    /**
     * Fat amount (in grams) snapshot.
     */
    @Column(nullable = false)
    private double fat;

    /**
     * Fiber amount (in grams) snapshot.
     */
    @Column(nullable = false)
    private double fiber;

    /**
     * Price (in euros) snapshot.
     */
    @Column(nullable = false)
    private double price;

    /**
     * Calculates the {@link Entry}'s nutrition and price snapshot values anew.
     */
    public abstract void snapshotNutritionAndPriceInfo();

    /**
     * Creates a new, unpersisted {@link Entry} with the same values in all fields except
     * {@code id}, {@code day}, {@code category}, {@code position}, which are intentionally
     * left unset.
     *
     * @return a new entry that is identical to the old one except for the id, day, category,
     * and position
     */
    public abstract Entry createDuplicate();

}