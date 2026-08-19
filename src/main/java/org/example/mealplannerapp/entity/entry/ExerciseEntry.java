package org.example.mealplannerapp.entity.entry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

import org.example.mealplannerapp.embeddable.ExerciseLevel;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Exercise;

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
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    /**
     * Duration of the referenced exercise in minutes.
     */
    @Column(nullable = true, precision = 5, scale = 2)
    private BigDecimal duration;

    /**
     * Name of intensity level selected for calories calculation.
     * If null or invalid, the entry's calories snapshot will be set to 0.
     */
    @Column(nullable = false, length = 10)
    private String intensityDesc;

    @Override
    public void snapshotNutritionAndPriceInfo() {

        BigDecimal caloriesPerMinute = getExercise().getLevels()
                .stream()
                .filter(l -> l.getIntensityDesc().equals(getIntensityDesc()))
                .findFirst()
                .map(ExerciseLevel::getCaloriesPerMinute)
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
        copy.setIntensityDesc(intensityDesc);
        copy.snapshotNutritionAndPriceInfo();

        return copy;
    }
}
