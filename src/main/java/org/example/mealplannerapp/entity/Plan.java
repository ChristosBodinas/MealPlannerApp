package org.example.mealplannerapp.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

import org.example.mealplannerapp.common.ActivityLevel;

import jakarta.persistence.*;
import lombok.*;

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

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)    // TODO: Cascade good or bad?
    @OrderBy("index ASC")
    private Set<Day> days;

    @Column(nullable = false, length = 50)
    private String name;

    /**
     * User weight (in kilograms) at the start of the plan.
     */
    @Column(name = "start_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal startWeight;

    /**
     * Amount of weight (in kilograms) user desires to lose by the end of the plan.
     * <p>Value can be set to zero or negative if user desires to maintain or gain weight instead.</p>
     */
    @Column(name = "desired_weight_loss", nullable = false, precision = 3, scale = 2)
    private BigDecimal desiredWeightLoss;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)  // TODO: length
    private ActivityLevel activityLevel;

    /**
     * Percentage of calories that should come from proteins.
     */
    @Column(name = "protein_ratio", nullable = false, precision = 3, scale = 2)
    private BigDecimal proteinRatio;

    /**
     * Percentage of calories that should come from carbohydrates.
     */
    @Column(name = "carbs_ratio", nullable = false, precision = 3, scale = 2)
    private BigDecimal carbsRatio;

    /**
     * Percentage of calories that should come from fat.
     */
    @Column(name = "fat_ratio", nullable = false, precision = 3, scale = 2)
    private BigDecimal fatRatio;

    /**
     * Target calorie intake (in Kcal) for the entire plan.
     */
    @Column(name = "target_calories", nullable = false, precision = 8, scale = 2)
    private BigDecimal targetCalories;

    /**
     * Target protein intake (in grams) for the entire plan.
     */
    @Column(name = "target_protein", nullable = false, precision = 6, scale = 2)
    private BigDecimal targetProtein;

    /**
     * Target carbohydrate intake (in grams) for the entire plan.
     */
    @Column(name = "target_carbs", nullable = false, precision = 6, scale = 2)
    private BigDecimal targetCarbs;

    /**
     * Target fat intake (in grams) for the entire plan.
     */
    @Column(name = "target_fat", nullable = false, precision = 6, scale = 2)
    private BigDecimal targetFat;

    public void computeNutritionGoals() {
        // Basal Metabolic Rate
        BigDecimal bmr = startWeight.multiply(new BigDecimal("10.0"))
                .add(user.getHeight().multiply(new BigDecimal("6.25")))
                .subtract(BigDecimal.valueOf((long) user.computeAgeInYears() * 5))
                .add(BigDecimal.valueOf(user.getSex().getBmrOffset()));

        // Total Daily Energy Expenditure
        BigDecimal tdee = bmr.multiply(BigDecimal.valueOf(activityLevel.getActivityFactor()));

        // Average Daily Deficit
        BigDecimal avgDeficit = desiredWeightLoss.multiply(BigDecimal.valueOf(7700))
                .divide(BigDecimal.valueOf(days.size()), RoundingMode.HALF_UP);

        // Plan Wide Nutrition Targets
        targetCalories = tdee.subtract(avgDeficit)
                .multiply(BigDecimal.valueOf(days.size()));
        targetProtein = targetCalories.multiply(proteinRatio)
                .divide(new BigDecimal(4), RoundingMode.HALF_UP);
        targetCarbs = targetCalories.multiply(carbsRatio)
                .divide(new BigDecimal(4), RoundingMode.HALF_UP);
        targetFat = targetCalories.multiply(BigDecimal.ONE.subtract(proteinRatio).subtract(carbsRatio))
                .divide(new BigDecimal(9), RoundingMode.HALF_UP);
    }

}
