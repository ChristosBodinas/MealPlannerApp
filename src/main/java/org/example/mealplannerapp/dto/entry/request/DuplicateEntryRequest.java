package org.example.mealplannerapp.dto.entry.request;

import jakarta.validation.constraints.NotNull;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.Entry;

/**
 * Request DTO for submitting {@link Entry} duplication data.
 */
public record DuplicateEntryRequest(

        @NotNull(message = "Source entry ID cannot be null.")
        Long entryId,

        @NotNull(message = "New entry category cannot be null.")
        Category category
) {
}
