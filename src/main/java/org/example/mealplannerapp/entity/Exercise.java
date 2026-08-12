package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.mealplannerapp.embeddable.ExerciseLevel;

import java.util.Set;

/**
 * An entity that represents information about a particular exercise.
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
     * User who owns the exercise.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Name or description of the exercise.
     */
    @Column(nullable = false, length = 45)
    private String name;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "exercise_level",
            joinColumns = @JoinColumn(name = "exercise_id"),
            uniqueConstraints = @UniqueConstraint(name = "UniqueNamePerExercise", columnNames = {"exercise_id", "name"}))
    @AttributeOverride(name = "name", column = @Column(nullable = false, length = 20))
    @AttributeOverride(name = "caloriesPerMinute", column = @Column(nullable = false))
    private Set<ExerciseLevel> levels;

}
