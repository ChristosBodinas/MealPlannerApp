package org.example.mealplannerapp.entity;

import java.util.Set;

import org.example.mealplannerapp.embeddable.ExerciseLevel;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false, length = 50)
    private String name;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "exercise_level", joinColumns = @JoinColumn(name = "exercise_id", nullable = false), 
        uniqueConstraints = @UniqueConstraint(name = "UniqueLevelPerName", columnNames = {"exercise_id", "level_name"}))
    @AttributeOverride(name = "levelName", column = @Column(name = "level_name", nullable = false, length = 10))
    @AttributeOverride(name = "caloriesPerMinute", column = @Column(name = "calories_per_minute", nullable = false, precision = 5, scale = 2))
    private Set<ExerciseLevel> levels;
    
}
