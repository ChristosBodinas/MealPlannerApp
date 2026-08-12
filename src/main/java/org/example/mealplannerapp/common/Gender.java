package org.example.mealplannerapp.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("Male", "He", "Him", "His", "His"),
    FEMALE("Female", "She", "Her", "Her", "Hers"),
    NON_BINARY("Non-Binary", "They", "Them", "Their", "Theirs");

    private final String displayName;
    private final String subjectPronoun;
    private final String objectPronoun;
    private final String possessiveAdjective;
    private final String possessivePronoun;
}