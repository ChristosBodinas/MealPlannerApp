package org.example.mealplannerapp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.plan.request.CreatePlanRequest;
import org.example.mealplannerapp.dto.plan.request.EditPlanRequest;
import org.example.mealplannerapp.dto.plan.response.ListedPlanResponse;
import org.example.mealplannerapp.dto.plan.response.PlanResponse;
import org.example.mealplannerapp.dto.plan.response.PlanSummaryResponse;
import org.example.mealplannerapp.dto.plan.response.ShopItemResponse;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.security.IdentityService;
import org.example.mealplannerapp.service.PlanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class PlanController {

    private final IdentityService identityService;
    private final PlanService planService;

    @PostMapping("/plans")
    public ResponseEntity<PlanResponse> createPlan(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePlanRequest request
    ) {
        User user = identityService.provisionFromJwt(jwt);
        PlanResponse response = planService.createPlan(user, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PatchMapping("/plans/{planId}")
    public ResponseEntity<PlanResponse> editPlanParameters(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId,
            @Valid @RequestBody EditPlanRequest request
    ) {
        User user = identityService.provisionFromJwt(jwt);
        PlanResponse response = planService.editPlanParameters(user, planId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<Void> deletePlan(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        planService.deletePlan(user, planId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/plans/{planId}")
    public ResponseEntity<PlanResponse> retrievePlan(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        PlanResponse response = planService.retrievePlan(user, planId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/plans")
    public ResponseEntity<Page<ListedPlanResponse>> searchPlans(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String searchText,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        User user = identityService.provisionFromJwt(jwt);
        Page<ListedPlanResponse> response = planService.searchPlans(user, searchText, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/plans/{planId}/summary")
    public ResponseEntity<PlanSummaryResponse> summarizePlan(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        PlanSummaryResponse response = planService.summarizePlan(user, planId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/plans/{planId}/shop_list")
    public ResponseEntity<List<ShopItemResponse>> generateShoppingList(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long planId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        List<ShopItemResponse> response = planService.generateShoppingList(user, planId);
        return ResponseEntity.ok(response);
    }
}
