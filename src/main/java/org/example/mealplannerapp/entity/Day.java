package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * An entity that represents a day in a given meal plan and the
 * target nutrition amounts for that day.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Day {

    // TODO: UniqueConstraints and/or CompositeKey using plan + position;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * Plan to which the day belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    /**
     * One-based position of the day within the plan.
     */
    @Column(nullable = false)
    private int position;

    /**
     * Target energy amount (in Kcal) for the day.
     */
    private double targetCalories;

    /**
     * Target protein amount (in grams) for the day.
     */
    private double targetProtein;

    /**
     * Target carbs amount (in grams) for the day.
     */
    private double targetCarbs;

    /**
     * Target fat amount (in grams) for the day.
     */
    private double targetFat;

    /**
     * Target fiber amount (in grams) for the day.
     */
    private double targetFiber;
    
}
