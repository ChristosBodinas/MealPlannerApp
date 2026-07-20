package org.example.mealplannerapp.controller;

import org.example.mealplannerapp.dto.plan.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.PlanEditRequest;
import org.example.mealplannerapp.dto.plan.PlanInfoResponse;
import org.example.mealplannerapp.dto.plan.PlanNutrientsResponse;
import org.example.mealplannerapp.security.AuthUser;
import org.example.mealplannerapp.service.PlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping("/plans")
    public ResponseEntity<PlanInfoResponse> createPlan(
        @AuthenticationPrincipal AuthUser authUser,
        @Valid @RequestBody PlanCreateRequest request
    ) {
        PlanInfoResponse response = planService.createPlan(authUser.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/plans/{planId}")
    public ResponseEntity<PlanInfoResponse> editPlan(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long planId,
        @Valid @RequestBody PlanEditRequest request
    ) {
        PlanInfoResponse response = planService.editPlan(authUser.getUser(), planId, request);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<PlanInfoResponse> retrievePlanInfo(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long planId
    ) {
        PlanInfoResponse response = planService.retrievePlanInfo(authUser.getUser(), planId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/plans/{planId}/totals")
    public ResponseEntity<PlanNutrientsResponse> retrievePlanTotals(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long planId
    ) {
        PlanNutrientsResponse response = planService.retrievePlanTotals(authUser.getUser(), planId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/plans/{planId}/goals")
    public ResponseEntity<PlanNutrientsResponse> retrievePlanGoals(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long planId
    ) {
        PlanNutrientsResponse response = planService.retrievePlanGoals(authUser.getUser(), planId);
        return ResponseEntity.ok(response);
    }

}
