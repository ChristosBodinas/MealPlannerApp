package org.example.mealplannerapp.entity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.mealplannerapp.embeddable.FoodPrice;
import org.example.mealplannerapp.embeddable.FoodUnit;

import jakarta.persistence.*;
import lombok.*;

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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    /**
     * Food's brand name.
     */
    @Column(nullable = true, length = 20)
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
     * Ratio of edible mass to total mass.
     */
    @Column(name = "edible_ratio", nullable = false, precision = 3, scale = 2)
    private BigDecimal edibleRatio;

    /**
     * Available reference units and their equivalents in grams.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "food_unit",
        joinColumns = @JoinColumn(name = "food_id", nullable = false),
        uniqueConstraints = @UniqueConstraint(name = "NoDuplicateUnitNamesPerFood", columnNames = {"food_id", "name"})
    )
    @AttributeOverride(name = "name", column = @Column(name = "name", nullable = false, length = 10))
    @AttributeOverride(name = "grams", column = @Column(name = "grams", nullable = false, precision = 5, scale = 2))
    Set<FoodUnit> units;

    /**
     * Available vendors and their pricing data.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "food_price",
        joinColumns = @JoinColumn(name = "food_id", nullable = false),
        uniqueConstraints = @UniqueConstraint(name = "NoDuplicateVendorNamesPerFood", columnNames = {"food_id", "vendor_name"})
    )
    @AttributeOverride(name = "vendorName", column = @Column(name = "vendor_name", nullable = false, length = 10))
    @AttributeOverride(name = "purchasePrice", column = @Column(name = "purchase_price", nullable = false, precision = 5, scale = 2))
    @AttributeOverride(name = "purchaseGrams", column = @Column(name = "purchase_grams", nullable = false, precision = 5, scale = 2))
    Set<FoodPrice> prices;

    public Map<String, BigDecimal> computePrices100g() {
        return prices.stream().collect(Collectors.toMap(
            FoodPrice::getVendorName,
            p -> p.getPurchasePrice().divide(p.getPurchaseGrams().multiply(BigDecimal.valueOf(100)))
        )); // TODO: Verify syntax.
    }
    
}
