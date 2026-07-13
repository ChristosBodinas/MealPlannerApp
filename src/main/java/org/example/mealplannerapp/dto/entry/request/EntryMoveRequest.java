package org.example.mealplannerapp.dto.entry.request;

import org.example.mealplannerapp.constants.Category;

public record EntryMoveRequest(
    Category category,
    int desiredPosition
) {
}
