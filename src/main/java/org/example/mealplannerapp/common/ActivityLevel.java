package org.example.mealplannerapp.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityLevel {
    SEDENTARY("Sedentary", 1.2),
    LIGHT("Lightly Active", 1.375),
    MODERATE("Moderately Active", 1.55),
    ACTIVE("Active", 1.725),
    INTENSE("Very Active", 1.9);

    private final String displayName;
    private final double activityFactor;
}