package org.example.mealplannerapp.dto.entry.request;

import lombok.Builder;

@Builder
public record EntryReorderRequest(
    Long entryId,
    Category category,
    int position
) {
}
