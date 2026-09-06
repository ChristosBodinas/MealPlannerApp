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

import java.math.BigDecimal;

/**
 * An {@link Entry }entity that represents a particular duration of a given {@link Exercise}
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
     * Exercise referenced by this entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    /**
     * Duration of the referenced exercise in minutes.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal duration;

    /**
     * Name of intensity level selected for calories calculation.
     * If null, the entry's calories snapshot will be set to 0.
     */
    @Column(length = 10)
    private String levelName;

    @Override
    public void snapshotInfo() {

        BigDecimal caloriesPerMinute = getExercise().getLevels()
                .stream()
                .filter(l -> l.getName().equals(levelName))
                .findFirst()
                .map(EffortLevel::getBurnRate)
                .orElse(BigDecimal.ZERO);

        setCalories(BigDecimal.ONE.negate()
                .multiply(caloriesPerMinute)
                .multiply(duration));

        setProtein(BigDecimal.ZERO);
        setCarbs(BigDecimal.ZERO);
        setFat(BigDecimal.ZERO);
        setFiber(BigDecimal.ZERO);
        setPrice(BigDecimal.ZERO);
    }

    @Override
    public ExerciseEntry createDuplicate() {
        ExerciseEntry copy = new ExerciseEntry();

        copy.setExercise(exercise);
        copy.setDuration(duration);
        copy.setLevelName(levelName);
        copy.snapshotInfo();

        return copy;
    }
}