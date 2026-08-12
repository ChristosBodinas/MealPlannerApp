package org.example.mealplannerapp.entity;

import java.util.Set;

import jakarta.persistence.*;
import lombok.*;
import org.example.mealplannerapp.common.ActivityLevel;
import org.example.mealplannerapp.projection.impl.GoalsImpl;


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
    @OrderBy("position ASC")
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
     * Desired loss in user weight.
     */
    @Column(nullable = false)
    private double targetLoss;

    /**
     * User's activity level throughout the entire plan.
     */
    @Column(nullable = false)
    private ActivityLevel activityLevel;

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

    public void computeNutritionGoals(double proteinRatio, double carbsRatio) {
        // Basal Metabolic Rate (Daily)
        double bmr = 10 * startWeight + 6.25 * user.getHeight() - 5 * user.deriveAgeInYears();
        bmr += user.getSex().getBmrOffset();

        // Total Daily Energy Expenditure (Daily)
        double tdee = bmr * activityLevel.getActivityFactor();

        // Target Loss in Kcal (Plan)
        double deficit = targetLoss * 7700;

        // Average Calorie Budget (Plan)
        double targetCalories = tdee * days.size() - deficit;

        // Macronutrient Goals (Plan)
        targetProtein = targetCalories * proteinRatio / 4;
        targetCarbs = targetCalories * carbsRatio / 4;
        targetFat = targetCalories * (1 - proteinRatio - carbsRatio) / 9;

        // Fiber Goal (Plan)
        targetFiber = user.getSex().getFiberIntake() * days.size();
    }

    public GoalsImpl retrieveGoals() {
        return new GoalsImpl(
            targetCalories,
            targetProtein,
            targetCarbs,
            targetFat,
            targetFiber
        );
    }

}
