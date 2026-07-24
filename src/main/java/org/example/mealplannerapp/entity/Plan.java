package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mealplannerapp.constants.ActivityLevel;

import java.util.List;

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

    // TODO: User values are used a lot to calculate stuff with Plan, so maybe it should be eager?
    /**
     * User who owns the plan.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Days that comprise the plan.
     */
    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @OrderColumn(name = "order")
    private List<Day> days;

    /**
     * Plan name.
     */
    @Column(nullable = false, length = 45)
    private String name;

    /**
     * User weight at the start of the plan.
     */
    @Column(nullable = false)
    private double startWeight;

    /**
     * Weight the user wants to gain or lose by the end of the plan.
     */
    @Column(nullable = false)
    private double desiredChange;

    /**
     * User activity level during the plan.
     */
    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private ActivityLevel activityLevel;

    /**
     * Percentage of calorie intake that should come from protein.
     */
    private double proteinRatio;

    /**
     * Percentage of calorie intake that should come from carbs.
     */
    private double carbsRatio;
}
