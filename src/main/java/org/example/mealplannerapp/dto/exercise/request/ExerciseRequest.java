package org.example.mealplannerapp.dto.exercise.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.example.mealplannerapp.entity.Exercise;

import java.util.Set;

/**
 * Request DTO for submitting {@link Exercise} creation/update data.
 */
@Builder
public record ExerciseRequest(

        @NotBlank(message = "Exercise name must contain at least 1 character.")
        @Size(max = 50, message = "Exercise name cannot exceed 50 characters.")
        String name,

        Set<@Valid LevelRequest> levels
) {
}
