package org.example.mealplannerapp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.exercise.request.ExerciseRequest;
import org.example.mealplannerapp.dto.exercise.response.ListedExerciseResponse;
import org.example.mealplannerapp.dto.exercise.response.ExerciseResponse;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.security.IdentityService;
import org.example.mealplannerapp.service.ExerciseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class ExerciseController {

    private final IdentityService identityService;
    private final ExerciseService exerciseService;

    @PostMapping("/exercises")
    public ResponseEntity<ExerciseResponse> createExercise(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ExerciseRequest request
    ) {
        User user = identityService.provisionFromJwt(jwt);
        ExerciseResponse response = exerciseService.createExercise(user, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/exercises/{exerciseId}")
    public ResponseEntity<ExerciseResponse> updateExercise(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long exerciseId,
            @Valid @RequestBody ExerciseRequest request
    ) {
        User user = identityService.provisionFromJwt(jwt);
        ExerciseResponse response = exerciseService.updateExercise(user, exerciseId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/exercises/{exerciseId}")
    public ResponseEntity<Void> deleteExercise(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long exerciseId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        exerciseService.deleteExercise(user, exerciseId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exercises/{exerciseId}")
    public ResponseEntity<ExerciseResponse> retrieveExercise(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long exerciseId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        ExerciseResponse response = exerciseService.retrieveExercise(user, exerciseId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/exercises")
    public ResponseEntity<Page<ListedExerciseResponse>> searchExercises(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String searchText,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        User user = identityService.provisionFromJwt(jwt);
        Page<ListedExerciseResponse> response = exerciseService.searchExercises(user, searchText, pageable);

        return ResponseEntity.ok(response);
    }
}
