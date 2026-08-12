package org.example.mealplannerapp.controller;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.day.response.DaySummaryResponse;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.security.AuthUser;
import org.example.mealplannerapp.service.DayService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class DayController {

    private final DayService dayService;

    @DeleteMapping("/days/{dayId}")
    public ResponseEntity<Void> deleteAllEntries(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long dayId
    ) {
        dayService.deleteAllEntries(authUser.getUser(), dayId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/days/{dayId}")
    public ResponseEntity<List<EntryResponse>> retrieveAllEntries(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long dayId
    ) {
        List<EntryResponse> responses = dayService.retrieveAllEntries(authUser.getUser(), dayId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/days/{dayId}/summary")
    public ResponseEntity<DaySummaryResponse> summarizeDay(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long dayId
    ) {
        DaySummaryResponse response = dayService.summarizeDay(authUser.getUser(), dayId);
        return ResponseEntity.ok(response);
    }

}
