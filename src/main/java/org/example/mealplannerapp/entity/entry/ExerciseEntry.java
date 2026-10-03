package org.example.mealplannerapp.entity.entry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.mealplannerapp.embeddable.EffortLevel;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Exercise;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

/**
 * An {@link Entry} that represents a particular duration of a given {@link Exercise}
 * logged in a particular {@link Day}.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExerciseEntry extends Entry {

    /**
     * The exercise reference by this entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Exercise exercise;

    /**
     * Duration of the referenced exercise in minutes.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal duration;

    /**
     * Name of effort level set for the referenced exercise.
     * If null, calories are set to 0.
     */
    @Column(name = "level_name", length = 10)
    private String levelName;

    @Override
    public void snapshotInfo() {
        // Snapshot entry name.
        setName(exercise.getName());

        // Snapshot entry nutrition values.
        BigDecimal burnRate = exercise.getLevels().stream()
                .filter(l -> l.getName().equals(levelName))
                .findFirst()
                .map(EffortLevel::getBurnRate)
                .orElse(BigDecimal.ZERO);

        setCalories(BigDecimal.ONE.negate()
                .multiply(duration)
                .multiply(burnRate));
        setProtein(BigDecimal.ZERO);
        setCarbs(BigDecimal.ZERO);
        setFat(BigDecimal.ZERO);
        setFiber(BigDecimal.ZERO);

        // Snapshot price.
        setPrice(BigDecimal.ZERO);
    }

    @Override
    public ExerciseEntry createDuplicate() {
        ExerciseEntry copy = new ExerciseEntry();

        copy.setExercise(exercise);
        copy.setDuration(duration);

        boolean levelStillValid = exercise.getLevels().stream()
                .map(EffortLevel::getName)
                .anyMatch(name -> name.equals(levelName));

        if (levelStillValid) {
            copy.setLevelName(levelName);
        } else {
            copy.setLevelName(null);
        }

        copy.snapshotInfo();
        return copy;
    }
}
