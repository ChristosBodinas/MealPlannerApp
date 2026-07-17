package org.example.mealplannerapp.dto.day;

import org.example.mealplannerapp.entity.Day;

import lombok.Builder;

@Builder
public record DayGoalsResponse(
    double caloriesGoal,
    double proteinGoal,
    double carbsGoal,
    double fatGoal,
    double fiberGoal
) {

    public static DayGoalsResponse from(Day day) {
        return new DayGoalsResponse(
            day.getCaloriesGoal(),
            day.getProteinGoal(),
            day.getCarbsGoal(),
            day.getFatGoal(),
            day.getFiberGoal());
    }

}
