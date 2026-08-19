package org.example.mealplannerapp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.security.IdentityService;
import org.example.mealplannerapp.service.FoodService;
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
public class FoodController {

    private final IdentityService identityService;
    private final FoodService foodService;

    @PostMapping("/foods")
    public ResponseEntity<FoodResponse> createFood(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody FoodRequest request
    ) {
        User user = identityService.provisionFromJwt(jwt);
        FoodResponse response = foodService.createFood(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/foods/{foodId}")
    public ResponseEntity<FoodResponse> updateFood(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long foodId,
            @Valid @RequestBody FoodRequest request
    ) {
        User user = identityService.provisionFromJwt(jwt);
        FoodResponse response = foodService.updateFood(user, foodId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/foods/{foodId}")
    public ResponseEntity<Void> deleteFood(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long foodId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        foodService.deleteFood(user, foodId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/foods/{foodId}")
    public ResponseEntity<FoodResponse> retrieveFood(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long foodId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        FoodResponse response = foodService.retrieveFood(user, foodId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/foods")
    public ResponseEntity<Page<ListedFoodResponse>> searchFoods(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String searchText,
            @PageableDefault(size = 20, sort = {"name", "brand"}, direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        User user = identityService.provisionFromJwt(jwt);
        Page<ListedFoodResponse> response = foodService.searchFoods(user, searchText, pageable);
        return ResponseEntity.ok(response);
    }
}
