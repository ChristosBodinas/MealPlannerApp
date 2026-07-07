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
public class FoodController {

    private final FoodService foodService;

    @PostMapping("/foods")
    public ResponseEntity<FoodResponse> createFood(
        @AuthenticationPrincipal AuthUser authUser,
        @Valid @RequestBody FoodRequest request
    ) {
        FoodResponse response = foodService.createFood(authUser.getUser(), request);
        return new ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/foods/{foodId}")
    public ResponseEntity<FoodResponse> updateFood(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long foodId,
        @Valid @RequestBody FoodRequest request
    ) {
        FoodResponse response = foodService.updateFood(authUser.getUser(), foodId, request);
        return new ResponseEntity.ok(response);
    }

    @DeleteMapping("/foods/{foodId}")
    public ResponseEntity<Void> deleteFood(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long foodId
    ) {
        foodService.deleteFood(authUser.g, foodId);
        return new ResponseEntity.noContent();
    }

    @GetMapping("/foods/{foodId}")
    public ResponseEntity<FoodResponse> retrieveFood(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long foodId
    ) {
        FoodResponse response = foodService.retrieveFood(authUser.getId(), foodId);
        return new ResponseEntity.ok(response);
    }

    @GetMapping("/foods")
    public ResponseEntity<List<ListedFoodResponse>> searchFoods(
        @AuthenticationPrincipal AuthUser authUser,
        @RequestParam String search
    ) {
        List<ListedFoodResponse> responses = foodService.searchFoods(authUser.getId(), search);
        return new ResponseEntity.ok(responses);
    }
}
