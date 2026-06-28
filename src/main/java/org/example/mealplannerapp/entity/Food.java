package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.mealplannerapp.embeddable.FoodPrice;
import org.example.mealplannerapp.embeddable.FoodUnit;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter @Setter @NoArgsConstructor
public class Food {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PROTECTED)
    private Long id;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 45)
    private String name;

    // Left this nullable to allow for generic foods.
    @Column (length = 45) private String brand;

    // Nutritional values are expressed per 100 grams of the given food.
    @Column(nullable = false) private double caloriesPer100g;
    @Column(nullable = false) private double proteinPer100g;
    @Column(nullable = false) private double carbsPer100g;
    @Column(nullable = false) private double fatPer100g;
    @Column(nullable = false) private double fiberPer100g;

    // This is used to calculate the actual price per 100g.
    @Column(nullable = false) private double edibleRatio;

    // Use @AttributesOverride to make the collection columns non-nullable, etc.
    // The user should be able to define additional reference units for each food.
    @ElementCollection
    @CollectionTable(
            name = "food_unit",
            joinColumns = @JoinColumn(name = "food_id"),
            uniqueConstraints = @UniqueConstraint(name = "UniqueNamePerFood", columnNames = {"food_id", "name"}))
    private Set<FoodUnit> units;

    // The user should be able to define prices for each seller that stocks a given food.
    @ElementCollection
    @CollectionTable(
            name = "food_price",
            joinColumns = @JoinColumn(name = "food_id"),
            uniqueConstraints = @UniqueConstraint(name = "UniqueSellerPerFood", columnNames = {"food_id", "seller"}))
    private Set<FoodPrice> prices;

    public Map<String, Double> calculatePricesPer100g() {
        return prices.stream().collect(Collectors.toMap(
                FoodPrice::getSeller,
                price -> 100 * price.getPurchasePrice() / (price.getPurchaseGrams() * edibleRatio)
        ));
    }
}
