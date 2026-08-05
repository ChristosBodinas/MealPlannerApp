package org.example.mealplannerapp.entity;

import java.util.HashSet;
import java.util.Set;

import org.example.mealplannerapp.embeddable.ExerciseIntensity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 45)
    private String name;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "exercise_intensity",
            joinColumns = @JoinColumn(name = "exercise_d"),
            uniqueConstraints = @UniqueConstraint(name = "UniqueNamePerExercise", columnNames = {"exercise_id", "name"}))
    @AttributeOverride(name = "name", column = @Column(nullable = false, length = 20))
    @AttributeOverride(name = "caloriesPerMinute", column = @Column(nullable = false))
    private Set<ExerciseIntensity> intensities = new HashSet<>();

    public Map<String, Double> mapIntensities () {
        return intensities.stream().collect(Collectors.toMap(
            ExerciseIntensity::getName,
            ExerciseIntensity::getCaloriesPerMinute
        ));
    }

}