package org.example.mealplannerapp.dto.entry.request;

import org.example.mealplannerapp.constants.Category;

public record EntryDuplicateRequest(
    Long entryId,
    Category category
) {
}
