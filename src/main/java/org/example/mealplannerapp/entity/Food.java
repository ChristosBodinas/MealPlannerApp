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
 * <p>Represents a food item and its associated nutritional, unit, and pricing data.
 * {@code Food} is the source-of-truth record referenced when creating {@code FoodEntry}
 * instances; entries snapshot nutrition data at creation time rather than referencing
 * this entity live, so later edits to a {@code Food} do not retroactively affect past entries.
 * <p>
 * The {@code units} and {@code prices} collections are guaranteed non-null and initialized
 * empty — see field-level notes for details, including why the uniqueness check depends on this.
 */
@Entity
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder    // for testing
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 45)
    private String name;

    // Nullable to allow for generic foods.
    @Column(length = 45)
    private String brand;

    /* NOTE:
    ALl nutritional and monetary values are represented using the double type.
    While double is known to be imprecise, the app's use case has no need for comparisons
    that would be affected by this imprecision, and any other numerical errors would be
    insignificant at the scale of budgeting groceries for one or two weeks.
     */

    // Nutritional values, expressed per 100 grams of the given food.
    @Column(nullable = false)
    private double caloriesPer100g;
    @Column(nullable = false)
    private double proteinPer100g;
    @Column(nullable = false)
    private double carbsPer100g;
    @Column(nullable = false)
    private double fatPer100g;
    @Column(nullable = false)
    private double fiberPer100g;

    // This field is used to calculate available prices per 100g of edible product.
    @Column(nullable = false)
    private double edibleRatio;

    /* NOTE:
    Maps were considered for the implementation of multiple available units and prices for each food.
    Ultimately, however, @Embeddable classes were preferred because Spring Validation does not innately
    support the validation of individual elements and fields within a map.
     */

    @ElementCollection
    @CollectionTable(
            name = "food_unit",
            joinColumns = @JoinColumn(name = "food_id"),
            uniqueConstraints = @UniqueConstraint(name = "UniqueNamePerFood", columnNames = {"food_id", "name"}))
    @AttributeOverride(name = "name", column = @Column(nullable = false, length = 20))
    @AttributeOverride(name = "grams", column = @Column(nullable = false))
    private Set<FoodUnit> units = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "food_price",
            joinColumns = @JoinColumn(name = "food_id"),
            uniqueConstraints = @UniqueConstraint(name = "UniqueMerchantPerFood", columnNames = {"food_id", "merchant"}))
    @AttributeOverride(name = "merchant", column = @Column(nullable = false, length = 20))
    @AttributeOverride(name = "purchasePrice", column = @Column(nullable = false))
    @AttributeOverride(name = "purchaseGrams", column = @Column(nullable = false))
    private Set<FoodPrice> prices = new HashSet<>();

    /**
     * <p>Calculates the price per 100 grams of edible product for each merchant.
     * </p>
     * @return all the merchant names and the corresponding calculated prices
     */
    public Map<String, Double> derivePricesPer100g() {
        return prices.stream().collect(Collectors.toMap(
                FoodPrice::getMerchant,
                p -> 100 * p.getPurchasePrice() / (p.getPurchaseGrams() * edibleRatio)
        ));
    }
}