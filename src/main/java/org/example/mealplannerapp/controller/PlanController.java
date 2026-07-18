package org.example.mealplannerapp.controller;

import org.example.mealplannerapp.dto.plan.PlanCreateRequest;
import org.example.mealplannerapp.dto.plan.PlanInfoResponse;
import org.example.mealplannerapp.security.AuthUser;
import org.example.mealplannerapp.service.PlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping("/plans")
    public ResponseEntity<PlanInfoResponse> createPlan(
        @AuthenticationPrincipal AuthUser authUser,
        @RequestBody PlanCreateRequest request
    ) {
        PlanInfoResponse response = planService.createPlan(authUser.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
}
