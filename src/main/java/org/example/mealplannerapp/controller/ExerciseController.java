package org.example.mealplannerapp.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.dto.exercise.response.ListedExerciseResponse;
import org.example.mealplannerapp.security.AuthUser;
import org.example.mealplannerapp.service.ExerciseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @PostMapping("/exercises")
    public ResponseEntity<ExerciseResponse> createExercise(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ExerciseRequest request
    ) {
        ExerciseResponse response = exerciseService.createExercise(authUser.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/exercises/{exerciseId}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long exerciseId,
            @Valid @RequestBody ExerciseRequest request
    ) {
        ExerciseResponse response = exerciseService.updateExercise(authUser.getUser(), exerciseId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/exercises/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long exerciseId
    ) {
        exerciseService.deleteExercise(authUser.getUser(), exerciseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exercises/{exerciseId}")
    public ResponseEntity<ExerciseResponse> retrieveExercise(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long exerciseId
    ) {
        ExerciseResponse response = exerciseService.retrievExercise(authUser.getUser(), exerciseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exercises")
    public ResponseEntity<List<ListedExerciseResponse>> searchExercises(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String search
    ) {
        List<ListedExerciseResponse> responses = exerciseService.searchExercises(authUser.getUser(), search);
        return ResponseEntity.ok(responses);
    }

}
