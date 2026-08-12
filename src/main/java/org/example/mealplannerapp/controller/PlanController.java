package org.example.mealplannerapp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.day.request.GoalsListRequest;
import org.example.mealplannerapp.dto.plan.request.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.request.PlanEditRequest;
import org.example.mealplannerapp.dto.plan.response.PlanResponse;
import org.example.mealplannerapp.dto.plan.response.ShoppingItemResponse;
import org.example.mealplannerapp.security.AuthUser;
import org.example.mealplannerapp.service.PlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping("/plans")
    public ResponseEntity<PlanResponse> createPlan(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PlanCreateRequest request
    ) {
        PlanResponse response = planService.createPlan(authUser.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/plans/{planId}")
    public ResponseEntity<PlanResponse> editPlan(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long planId,
            @Valid @RequestBody PlanEditRequest request
    ) {
        PlanResponse response = planService.editPlan(authUser.getUser(), planId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/plans/{planId}/redist")
    public ResponseEntity<Void> redistributeGoals(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long planId,
            @Valid @RequestBody GoalsListRequest request
    ) {
        planService.redistributeGoals(authUser.getUser(), planId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<Void> deletePlan(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long planId
    ) {
        planService.deletePlan(authUser.getUser(), planId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/plans/{planId}")
    public ResponseEntity<PlanResponse> retrievePlan(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long planId
    ) {
        PlanResponse response = planService.retrievePlan(authUser.getUser(), planId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/plans/{planId}/list")
    public ResponseEntity<List<ShoppingItemResponse>> generateShoppingList(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long planId
    ) {
        List<ShoppingItemResponse> response = planService.generateShoppingList(authUser.getUser(), planId);
        return ResponseEntity.ok(response);
    }

}
