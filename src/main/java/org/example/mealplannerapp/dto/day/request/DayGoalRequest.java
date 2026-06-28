package org.example.mealplannerapp.dto.day.request;

public record DayGoalRequest(
    double caloriesGoal,
    double proteinGoal,
    double carbsGoal,
    double fatGoal,
    double fiberGoal
) {}