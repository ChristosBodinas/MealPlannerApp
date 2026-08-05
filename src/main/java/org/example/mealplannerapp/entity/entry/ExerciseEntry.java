package org.example.mealplannerapp.entity.entry;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;

import java.util.Map;

/**
 * An {@link Entry }entity that represents a particular duration of a single {@link Exercise}
 * logged in a particular {@link Day}.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FoodEntry extends Entry {

    /**
     * Food referenced by this entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    /**
     * Duration of the given exercise in minutes.
     */
    @Column(nullable = false)
    private double minutes;

    /**
     * Name of intensity selected.
     */
    @Column(nullable = false, length = 20)
    private String intensity;

    @Override
    public void snapshotNutritionAndPriceInfo() {
        
        Map<String, Double> intensities = exercise.mapIntensities();
        if (intensity != null && intensities.containsKey(intensity)) {
            setCalories(-intensities.get(intensity) * duration);
        } else {
            setCalories(0.0);
        }

        // TODO: Modify protein too?
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
        copy.setIntensity(intensity);
        copy.snapshotNutritionAndPriceInfo();

        return copy;
    }
}