package org.example.mealplannerapp.dto.exercise.request;

import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record ExerciseRequest(

    @NotBlank(message = "Exercise name must contain at least 1 character.")
    @Size(max = 50, message = "Exercise name cannot exceed 50 characters.")
    String name,

    Set<@Valid LevelRequest> levels
) {    
}
