package org.example.mealplannerapp.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.entry.request.DuplicateEntryRequest;
import org.example.mealplannerapp.dto.entry.request.create.CreateEntryRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.security.IdentityService;
import org.example.mealplannerapp.service.EntryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class EntryController {

    private final IdentityService identityService;
    private final EntryService entryService;

    @PostMapping("/days/{dayId}/entries")
    public ResponseEntity<EntryResponse> createEntry(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long dayId,
            @Valid @RequestBody CreateEntryRequest request
    ) {
        User user = identityService.provisionFromJwt(jwt);
        EntryResponse response = entryService.createEntry(user, dayId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/days/{dayId}/entries/paste")
    public ResponseEntity<EntryResponse> duplicateEntry(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long dayId,
            @Valid @RequestBody DuplicateEntryRequest request
    ) {
        User user = identityService.provisionFromJwt(jwt);
        EntryResponse response = entryService.duplicateEntry(user, dayId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/entries/{entryId}")
    public ResponseEntity<Void> deleteEntry(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long entryId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        entryService.deleteEntry(user, entryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entries/{entryId}")
    public ResponseEntity<EntryResponse> retrieveEntry(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long entryId
    ) {
        User user = identityService.provisionFromJwt(jwt);
        EntryResponse response = entryService.retrieveEntry(user, entryId);
        return ResponseEntity.ok(response);
    }
}
