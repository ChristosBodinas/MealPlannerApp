package org.example.mealplannerapp.entity.entry;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.Day;

import java.math.BigDecimal;

/**
 * Base class for all entry entities. Implemented via joined inheritance.
 * Includes positioning data and nutrition/price snapshots calculated on
 * entry creation and update.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
// TODO: Decide whether to keep the unique constraint.
@Table(uniqueConstraints = @UniqueConstraint(name="UniquePositionPerDayAndCategory", columnNames = {"day_id", "category", "position"}))
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
     * Day which contains the entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private Day day;

    /**
     * Category which contains the entry.
     */
    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private Category category;

    /**
     * One-based position of the entry within its day and category.
     */
    @Column(nullable = false)
    private int position;

    /**
     * Name of the entry. Snapshot on entry creation/update.
     */
    @Column(nullable = false, length = 75)
    private String name;

    /**
     * Amount (in Kcal) of calories. Snapshot on entry creation/update.
     */
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal calories;

    /**
     * Amount (in grams) of protein. Snapshot on entry creation/update.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal protein;

    /**
     * Amount (in grams) of carbohydrates. Snapshot on entry creation/update.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal carbs;

    /**
     * Amount (in grams) of fat. Snapshot on entry creation/update.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal fat;

    /**
     * Amount (in grams) of fiber. Snapshot on entry creation/update.
     */
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal fiber;

    /**
     * Price (in euros). Snapshot on entry creation/update.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal price;

    /**
     * Calculates the {@link Entry}'s name, nutrition values, and price anew.
     */
    public abstract void snapshotInfo();

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
