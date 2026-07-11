package org.example.mealplannerapp.dto.entry.request;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record EntriesReorderRequest(

    @NotEmpty(message = "At least one entry must be reordered for this operation.")
    Set<@Valid EntryReorderRequest> requests

) {
}
