package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mealplannerapp.embeddable.FoodPrice;
import org.example.mealplannerapp.embeddable.FoodUnit;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An entity that represents a food's general information, nutritional values,
 * reference units, and pricing data.
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(nullable = false, length = 45)
    private String name;

    // This is left as nullable to allow for generic foods.
    @Column(length = 45)
    private String brand;

    /**
     * Energy amount (in Kcal) per 100 grams of food.
     */
    @Column(nullable = false)
    private double caloriesPer100g;

    /**
     * Protein amount (in grams) per 100 grams of food.
     */
    @Column(nullable = false)
    private double proteinPer100g;

    /**
     * Carbonhydrate amount (in grams) per 100 grams of food.
     */
    @Column(nullable = false)
    private double carbsPer100g;

    /**
     * Fat amount (in grams) per 100 grams of food.
     */
    @Column(nullable = false)
    private double fatPer100g;

    /**
     * Grams of fiber per 100 grams of food.
     */
    @Column(nullable = false)
    private double fiberPer100g;

    /**
     * Percentage of the food that is actually edible. Used in price calculations.
     */
    @Column(nullable = false)
    private double edibleRatio;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "food_unit",
            joinColumns = @JoinColumn(name = "food_id"),
            uniqueConstraints = @UniqueConstraint(name = "UniqueNamePerFood", columnNames = {"food_id", "name"}))
    @AttributeOverride(name = "name", column = @Column(nullable = false, length = 20))
    @AttributeOverride(name = "grams", column = @Column(nullable = false))
    private Set<FoodUnit> units = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "food_price",
            joinColumns = @JoinColumn(name = "food_id"),
            uniqueConstraints = @UniqueConstraint(name = "UniqueVendorPerFood", columnNames = {"food_id", "vendor"}))
    @AttributeOverride(name = "vendor", column = @Column(nullable = false, length = 20))
    @AttributeOverride(name = "purchasePrice", column = @Column(nullable = false))
    @AttributeOverride(name = "purchaseGrams", column = @Column(nullable = false))
    private Set<FoodPrice> prices = new HashSet<>();

    /**
     * Calculates the price per 100 grams of edible product for each vendor.
     * @return all the vendor names along with the corresponding prices
     */
    public Map<String, Double> derivePricesPer100g() {
        return prices.stream().collect(Collectors.toMap(
                FoodPrice::getVendor,
                p -> 100 * p.getPurchasePrice() / (p.getPurchaseGrams() * edibleRatio)
        ));
    }
}