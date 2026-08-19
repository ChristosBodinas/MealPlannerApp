package org.example.mealplannerapp.entity.entry;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.Day;

// TODO: Review docs and unique constraint.

/**
 * Base class for all entry entities. Implemented via joined inheritance.
 * Includes positioning data and nutrition/price snapshots calculated on
 * entry creation and update.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UniquePositionPerDayAndCategory", columnNames = {"day_id", "category", "position"}))
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
     * Day that contains the entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private Day day;

    /**
     * Category to which the entry belongs.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Category category;

    /**
     * One-based position of the entry within its category and scoped to its day.
     */
    @Column(nullable = false)
    private int position;

    /**
     * Amount (in Kcal) of calories, snapshotted on entry creation/update.
     */
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal calories;

    /**
     * Amount (in grams) of protein, snapshotted on entry creation/update.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal protein;

    /**
     * Amount (in grams) of carbohydrates, snapshotted on entry creation/update.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal carbs;

    /**
     * Amount (in grams) of fat, snapshotted on entry creation/update.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal fat;

    /**
     * Amount (in grams) of fiber, snapshotted on entry creation/update.
     */
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal fiber;

    /**
     * Price (in euros) snapshot.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal price;

    /**
     * Calculates the {@link Entry}'s nutrition and price snapshot values anew.
     */
    public abstract void snapshotNutritionAndPriceInfo();

    /**
     * Creates a new, unpersisted {@link Entry} with the same values in all fields except
     * {@code id}, {@code day}, {@code category}, {@code position}, which are intentionally
     * left unset.
     * @return a new entry that is identical to the old one except for the id, day, category,
     * and position
     */
    public abstract Entry createDuplicate();
}