package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mealplannerapp.embeddable.EffortLevel;

import java.util.Set;

/**
 * An entity that represents a single exercise.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    /**
     * User who created and owns this exercise.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    /**
     * Effort levels available for this exercise, and their calorie burn rates.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "effort_level",
            joinColumns = @JoinColumn(name = "exercise_id", nullable = false),
            uniqueConstraints = @UniqueConstraint(name = "UniqueLevelNamePerExercise", columnNames = {"exercise_id", "name"}))
    @AttributeOverride(name = "name", column = @Column(nullable = false, length = 10))
    @AttributeOverride(name = "burnRate", column = @Column(name = "burn_rate", nullable = false, precision = 5, scale = 2))
    private Set<EffortLevel> levels;
}
