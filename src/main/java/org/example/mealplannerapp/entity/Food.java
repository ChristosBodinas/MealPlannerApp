package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mealplannerapp.embeddable.ReferenceUnit;
import org.example.mealplannerapp.embeddable.VendorData;

import java.math.BigDecimal;
import java.util.Set;

/**
 * An entity that represents a particular food, including its
 * nutritional and pricing information.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * Name of the user who created and owns this food.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String brand;

    /**
     * Amount (in Kcal) of calories per 100 grams of edible mass.
     */
    @Column(name = "calories_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal calories100g;

    /**
     * Amount (in grams) of protein per 100 grams of edible mass.
     */
    @Column(name = "protein_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal protein100g;

    /**
     * Amount (in grams) of carbohydrates per 100 grams of edible mass.
     */
    @Column(name = "carbs_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal carbs100g;

    /**
     * Amount (in grams) of fat per 100 grams of edible mass.
     */
    @Column(name = "fat_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal fat100g;

    /**
     * Amount (in grams) of fiber per 100 grams of edible mass.
     */
    @Column(name = "fiber_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal fiber100g;
    // NOTE: Precision value chosen for consistency, even though fiber is unlikely to need more than 4.

    /**
     * Radio of edible mass to total food mass.
     */
    @Column(name = "edible_ratio", nullable = false, precision = 3, scale = 2)
    private BigDecimal edibleRatio;

    /**
     * Reference units available for this food, and their equivalents in grams.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "reference_unit",
            joinColumns = @JoinColumn(name = "food_id", nullable = false),
            uniqueConstraints = @UniqueConstraint(name = "UniqueUnitNamePerFood", columnNames = {"food_id", "name"}))
    @AttributeOverride(name = "name", column = @Column(nullable = false, length = 10))
    @AttributeOverride(name = "grams", column = @Column(nullable = false, precision = 6, scale = 2))
    private Set<ReferenceUnit> units;

    /**
     * Vendors available for this food, and their pricing data.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "vendor_data",
            joinColumns = @JoinColumn(name = "food_id", nullable = false),
            uniqueConstraints = @UniqueConstraint(name = "UniqueVendorNamePerFood", columnNames = {"food_id", "name"}))
    @AttributeOverride(name = "name", column = @Column(nullable = false, length = 10))
    @AttributeOverride(name = "purchasePrice", column = @Column(name = "purchase_price", nullable = false, precision = 5, scale = 2))
    @AttributeOverride(name = "purchaseGrams", column = @Column(name = "purchase_grams", nullable = false, precision = 6, scale = 2))
    private Set<VendorData> vendors;

}
