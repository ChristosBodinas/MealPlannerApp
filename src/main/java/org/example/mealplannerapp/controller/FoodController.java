package org.example.mealplannerapp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.security.AuthUser;
import org.example.mealplannerapp.service.FoodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

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
    public ResponseEntity<FoodResponse> updateFoodById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long foodId,
            @RequestBody FoodRequest request
    ) {
        return ResponseEntity.ok(
                foodService.updateFoodById(authUser.getUser(), foodId, request));
    }

    @DeleteMapping("/{foodId}")
    public ResponseEntity<Void> deleteFoodById(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long foodId
    ) {
        foodService.deleteFoodById(authUser.getUser(), foodId);
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<Set<FoodResponse>> retrieveFoodByTextSearch(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String text
    ) {
        return ResponseEntity.ok(
                foodService.retrieveFoodsByTextSearch(authUser.getUser(), text));
    }

    @GetMapping
    public ResponseEntity<Set<FoodResponse>> retrieveAllFoods(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(
                foodService.retrieveAllFoods(authUser.getUser()));
    }
}
