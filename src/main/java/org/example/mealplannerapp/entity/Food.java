package org.example.mealplannerapp.entity;

import java.math.BigDecimal;
import java.util.Set;

import org.example.mealplannerapp.embeddable.FoodUnit;

import jakarta.persistence.*;
import lombok.*;

/**
 * An entity that represents a particular food's name, brand,
 * nutritional information, and pricing data.
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
     * User who owns the food.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 30)
    private String brand;

    /**
     * Amount (in Kcal) of calories per 100 grams of food.
     */
    @Column(name = "calories_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal calories100g;

    /**
     * Amount (in grams) of protein per 100 grams of food.
     */
    @Column(name = "protein_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal protein100g;

    /**
     * Amount (in grams) of carbohydrates per 100 grams of food.
     */
    @Column(name = "carbs_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal carbs100g;

    /**
     * Amount (in grams) of fat per 100 grams of food.
     */
    @Column(name = "fat_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal fat100g;

    /**
     * Amount (in grams) of fiber per 100 grams of food.
     */
    @Column(name = "fiber_per_100g", nullable = false, precision = 6, scale = 2)
    private BigDecimal fiber100g;

    /**
     * Ratio of edible mass per 100 grams of food.
     */
    @Column(name = "edible_ratio", nullable = false, precision = 3, scale = 2)
    private BigDecimal edibleRatio;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "food_unit", joinColumns = @JoinColumn(name = "food_id"),
     uniqueConstraints = @UniqueConstraint(name="UniqueUnitPerFood", columnNames = {"food_id", "unit_name"}))
    @AttributeOverride(name = "unitName", column = @Column(name = "unit_name", nullable = false, length = 10))
    @AttributeOverride(name = "grams", column = @Column(name = "grams_in_unit", nullable = false))
    private Set<FoodUnit> units;

    // TODO: unit and price
    
}
