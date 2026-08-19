package org.example.mealplannerapp.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("Male", "he/him"),
    FEMALE("Female", "she/her"),
    NON_BINARY("Non-Binary", "they/them");

    private final String displayName;
    private final String pronouns;
}
