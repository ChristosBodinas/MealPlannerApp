package org.example.mealplannerapp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.security.AuthUser;
import org.example.mealplannerapp.service.FoodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class FoodController {

    private final FoodService foodService;

    @PostMapping("/foods")
    public ResponseEntity<FoodResponse> createFood(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody FoodRequest request
    ) {
        FoodResponse response = foodService.createFood(authUser.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/foods/{foodId}")
    public ResponseEntity<FoodResponse> updateFood(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long foodId,
            @Valid @RequestBody FoodRequest request
    ) {
        FoodResponse response = foodService.updateFood(authUser.getUser(), foodId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/foods/{foodId}")
    public ResponseEntity<Void> deleteFood(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long foodId
    ) {
        foodService.deleteFood(authUser.getUser(), foodId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/foods/{foodId}")
    public ResponseEntity<FoodResponse> retrieveFood(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long foodId
    ) {
        FoodResponse response = foodService.retrieveFood(authUser.getUser(), foodId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/foods")
    public ResponseEntity<List<ListedFoodResponse>> searchFoods(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String search
    ) {
        List<ListedFoodResponse> responses = foodService.searchFoods(authUser.getUser(), search);
        return ResponseEntity.ok(responses);
    }

}
