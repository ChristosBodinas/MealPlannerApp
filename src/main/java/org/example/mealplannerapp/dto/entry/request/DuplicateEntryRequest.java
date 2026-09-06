package org.example.mealplannerapp.dto.entry.request;

import jakarta.validation.constraints.NotNull;
import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.entry.Entry;

/**
 * Request DTO for submitting {@link Entry} duplication data.
 */
public record DuplicateEntryRequest(

        @NotNull(message = "A source entry identifier must be provided for duplication.")
        Long entryId,

        @NotNull(message = "A target category must be provided for the duplicated entry.")
        Category category
) {
}
