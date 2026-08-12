package org.example.mealplannerapp.entity;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;


/**
 * An entity that represents a meal plan and its overall parameters.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * User who owns the plan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Days that comprise the plan.
     */
    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @OrderBy("position ASC")    // TODO: Possibly unnecessary/ineffectual when using sets.
    private Set<Day> days;

    /**
     * Plan name.
     */
    @Column(nullable = false, length = 45)
    private String name;

    /**
     * Initial user weight for calculating nutritional goals.
     */
    @Column(nullable = false)
    private double startWeight;

    /**
     * Desired change in user weight.
     */
    @Column(nullable = false)
    private double targetChange;

    // TODO: Activity Levels

    /**
     * Target amount (in Kcal) of calories across the entire plan.
     */
    @Column(nullable = false)
    private double targetCalories;

    /**
     * Target amount (in grams) of protein across the entire plan.
     */
    @Column(nullable = false)
    private double targetProtein;

    /**
     * Target amount (in grams) of carbs across the entire plan.
     */
    @Column(nullable = false)
    private double targetCarbs;

    /**
     * Target amount (in grams) of fat across the entire plan.
     */
    @Column(nullable = false)
    private double targetFat;

    /**
     * Target amount (in grams) of fiber across the entire plan.
     */
    @Column(nullable = false)
    private double targetFiber;

    public computeNutritionGoals() {
        // Basal Metabolic Rate
        double bmr = 10 * startWeight + 6.25 * user.getHeight() - 5 * user.deriveAgeInYears();
        bmr += user.getSex().getBmrOffset();

        // TODO: Write the rest of the method.

    }

}
