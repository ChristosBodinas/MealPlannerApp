package org.example.mealplannerapp.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UniqueIndexPerPlan", columnNames = {"plan", "index"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Day {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * Plan that contains the day.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    /**
     * One-based within the plan. Cannot be reordered.
     */
    @Column(nullable = false)
    private int index;

    /**
     * Target calorie intake (in Kcal) for the day.
     */
    @Column(name = "target_calories", nullable = false, precision = 6, scale = 2)
    private BigDecimal targetCalories;

    /**
     * Target protein intake (in grams) for the day.
     */
    @Column(name = "target_protein", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetProtein;

    /**
     * Target carbohydrate intake (in grams) for the day.
     */
    @Column(name = "target_carbs", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetCarbs;

    /**
     * Target fat intake (in grams) for the day.
     */
    @Column(name = "target_fat", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetFat;

    /**
     * Target fiber intake (in grams) for the day.
     */
    @Column(name = "target_fiber", nullable = false, precision = 4, scale = 2)
    private BigDecimal targetFiber;

}