package org.example.mealplannerapp.dto.entry.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import org.example.mealplannerapp.constants.Category;

/**
 * Request DTO for duplicating entries.
 */
@Builder
public record EntryDuplicateRequest(

        @NotNull(message = "A source entry id must be provided for duplication.")
        Long entryId,

        @NotNull(message = "A target category for the duplicate entry must be provided.")
        Category category
) {
}