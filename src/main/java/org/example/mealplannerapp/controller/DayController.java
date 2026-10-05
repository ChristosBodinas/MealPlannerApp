package org.example.mealplannerapp.controller;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.day.response.DaySummaryResponse;
import org.example.mealplannerapp.dto.entry.response.listed.ListedEntryResponse;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.security.IdentityService;
import org.example.mealplannerapp.service.DayService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class DayController {

    private final IdentityService identityService;
    private final DayService dayService;

    @DeleteMapping("/days/{dayId}")
    public ResponseEntity<Void> deleteAllEntries(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long dayId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        dayService.deleteAllEntries(user, dayId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/days/{dayId}")
    public ResponseEntity<List<ListedEntryResponse>> retrieveAllEntries(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long dayId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        List<ListedEntryResponse> response = dayService.retrieveAllEntries(user, dayId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/days/{dayId}/summary")
    public ResponseEntity<DaySummaryResponse> summarizeDay(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long dayId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        DaySummaryResponse response = dayService.summarizeDay(user, dayId);
        return ResponseEntity.ok(response);
    }
}
