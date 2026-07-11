package org.example.mealplannerapp.dto.entry.request;

import lombok.Builder;
import org.example.mealplannerapp.constants.Category;

@Builder
public record EntryReorderRequest(
    Long entryId,
    Category category,
    int position
) {
}
