package org.example.mealplannerapp.dto.entry.request;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;

public record EntryBulkRequest(
    @NotEmpty(message = "At least one entry must be selected for this operation.")
    Set<Long> entryIds
) {
}
