package org.example.mealplannerapp.entity.entry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
    @Column(nullable = true)
    private double duration;

    /**
     * Name of intensity level selected for calories calculation.
     * If null or invalid, the entry's calories snapshot will be set to 0.
     */
    @Column(nullable = false, length = 20)
    private String level;

    @Override
    public void snapshotNutritionAndPriceInfo() {

        double caloriesPerMinute = getExercise().getLevels()
            .stream()
            .filter(l -> l.getName().equals(getLevel()))
            .findFirst()
            .map(ExerciseLevel::getCaloriesPerMinute)
            .orElse(0.0);

        setCalories(-1 * caloriesPerMinute * duration);
        setProtein(0.0);
        setCarbs(0.0);
        setFat(0.0);
        setFiber(0.0);
        setPrice(0.0);
    }

    @Override
    public ExerciseEntry createDuplicate() {
        ExerciseEntry copy = new ExerciseEntry();

        copy.setExercise(exercise);
        copy.setDuration(duration);
        copy.setLevel(level);
        copy.snapshotNutritionAndPriceInfo();

        return copy;
    }
}