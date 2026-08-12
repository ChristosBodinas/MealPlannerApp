package org.example.mealplannerapp.dto.exercise.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record ExerciseRequest(

        @NotBlank(message = "Exercise name cannot be blank.")
        @Size(max = 45, message = "Exercise name cannot exceed 45 characters.")
        String name,

        Set<@Valid LevelRequest> levels
) {
}