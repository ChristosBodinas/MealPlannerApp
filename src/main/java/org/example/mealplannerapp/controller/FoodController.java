package org.example.mealplannerapp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.FoodRequest;
import org.example.mealplannerapp.dto.FoodResponse;
import org.example.mealplannerapp.security.AuthUser;
import org.example.mealplannerapp.service.FoodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/foods")
public class FoodController {

    private final FoodService foodService;

    // TO DO: Implement validation!!!

    @PostMapping
    public ResponseEntity<FoodResponse> createNewFood(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody FoodRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                foodService.createNewFood(authUser.getUser(), request));
    }

    @PutMapping("/{foodId}")
    public ResponseEntity<FoodResponse> updateExistingFood(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long foodId,
            @Valid @RequestBody FoodRequest request
    ) {
        return ResponseEntity.ok(
                foodService.updateExistingFood(authUser.getUser(), foodId, request));
    }

    @GetMapping("/{foodId}")
    public ResponseEntity<FoodResponse> retrieveFoodById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long foodId
    ) {
        return ResponseEntity.ok(
                foodService.retrieveFoodById(authUser.getUser(), foodId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FoodResponse>> retrieveFoodsByTextSearch(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String search
    ) {
        return ResponseEntity.ok(
                foodService.retrieveFoodsByTextSearch(authUser.getUser(), search));
    }

    @GetMapping
    public ResponseEntity<List<FoodResponse>> retrieveAllFoods(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(
                foodService.retrieveAllFoods(authUser.getUser()));
    }

    @DeleteMapping("/{foodId}")
    public ResponseEntity<Void> deleteFoodById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long foodId
    ) {
        foodService.deleteFoodById(authUser.getUser(), foodId);
        return ResponseEntity.noContent().build();
    }
}
