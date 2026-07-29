package org.example.mealplannerapp.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.entry.request.EntryDuplicateRequest;
import org.example.mealplannerapp.dto.entry.request.EntryMoveRequest;
import org.example.mealplannerapp.dto.entry.request.create.EntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.security.AuthUser;
import org.example.mealplannerapp.service.EntryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/days/{dayId}/entries/paste")
    public ResponseEntity<EntryResponse> duplicateEntry(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long dayId,
            @Valid @RequestBody EntryDuplicateRequest request
    ) {
        EntryResponse response = entryService.duplicateEntry(authUser.getUser(), dayId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/entries/{entryId}")
    public ResponseEntity<EntryResponse> editEntry(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long entryId,
            @Valid @RequestBody EntryEditRequest request
    ) {
        EntryResponse response = entryService.editEntry(authUser.getUser(), entryId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/days/{dayId}/entries/{entryId}")
    public ResponseEntity<Void> moveEntry(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long dayId,
            @PathVariable Long entryId,
            @Valid @RequestBody EntryMoveRequest request
    ) {
        entryService.moveEntry(authUser.getUser(), dayId, entryId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/entries/{entryId}")
    public ResponseEntity<Void> deleteEntry(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long entryId
    ) {
        entryService.deleteEntry(authUser.getUser(), entryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entries/{entryId}")
    public ResponseEntity<EntryResponse> retrieveEntry(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long entryId
    ) {
        EntryResponse response = entryService.retrieveEntry(authUser.getUser(), entryId);
        return ResponseEntity.ok(response);
    }

}
