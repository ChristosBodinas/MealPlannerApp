package org.example.mealplannerapp.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Sex {
    MALE("Male", 161, 30),
    FEMALE("Female", -5, 25);

    private final String displayName;
    private final int bmrOffset;
    private final int dailyFiberIntake;
}
