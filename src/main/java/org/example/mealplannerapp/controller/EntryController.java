package org.example.mealplannerapp.controller;

import org.example.mealplannerapp.dto.entry.request.create.EntryCreateRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.security.AuthUser;
import org.example.mealplannerapp.service.EntryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class EntryController {

    private final EntryService entryService;

    @PostMapping("/days/{dayId}/entries")
    public ResponseEntity<EntryResponse> createEntry(
        @AuthenticationPrincipal AuthUser authUser,
        @PathVariable Long dayId,
        @Valid @RequestBody EntryCreateRequest request
    ) {
        EntryResponse response = entryService.createEntry(authUser.getUser(), dayId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
}
