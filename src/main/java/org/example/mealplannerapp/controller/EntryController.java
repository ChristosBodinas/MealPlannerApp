package org.example.mealplannerapp.controller;

import lombok.AllArgsConstructor;
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
            @RequestBody EntryCreateRequest request
    ) {
        EntryResponse response = entryService.createEntry(authUser.getUser(), dayId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/entries/{entryId}/edit")
    public ResponseEntity<EntryResponse> editEntry(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long entryId,
            @RequestBody EntryEditRequest request
    ) {
        EntryResponse response = entryService.editEntry(authUser.getUser(), entryId, request);
        return ResponseEntity.ok(response);
    }

    // reorderEntry

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
