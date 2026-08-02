package org.example.mealplannerapp.controller;

import org.example.mealplannerapp.security.AuthUser;
import org.example.mealplannerapp.service.DayService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

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
    
}
