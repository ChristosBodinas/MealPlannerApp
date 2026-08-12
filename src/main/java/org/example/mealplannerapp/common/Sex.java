package org.example.mealplannerapp.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Sex {
    MALE("Male", 161.0, 30.0),
    FEMALE("Female", -5.0, 25.0);

    private final String displayName;
    private final double bmrOffset;
    private final double fiberIntake;
}