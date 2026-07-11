package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.mealplannerapp.dto.entry.request.EntriesReorderRequest;
import org.example.mealplannerapp.dto.entry.request.EntryBulkRequest;
import org.example.mealplannerapp.dto.entry.request.EntryReorderRequest;
import org.example.mealplannerapp.dto.entry.request.create.EntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.IllegalDuplicateValueException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



// TO DO: Implement validation for displayUnit and displayMerchant
// TO DO: Implement validation for all entry dtos.

@Service
@AllArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final EntryMapper entryMapper;

    private final DayRepository dayRepository;
    private final FoodRepository foodRepository;

    public EntryResponse createEntryInDay(User user, Long dayId, EntryCreateRequest request) {
        Entry entry = entryMapper.createFromRequest(request);

        Day day = dayRepository.findByIdVerified(user.getId(), dayId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested day (id: " + dayId + ") not found."));
        entry.setDay(day);

        switch (request) {
            case FoodEntryCreateRequest fRequest:
                Food food = foodRepository.findByIdVerified(user.getId(), fRequest.foodId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Requested food (id: " + fRequest.foodId() + ") not found."));
                ((FoodEntry) entry).setFood(food);
                break;
        }

        entry.snapshotNutritionAndPriceInfo();

        Entry saved = entryRepository.save(entry);
        return entryMapper.generateResponse(saved);
    }

    // (POST) duplicateEntries(User user, Long dayId, List<Long> entryIds)
    // what happens if not all entries are found?
    // how to make actual bulk operations?

    @Transactional
    public EntryResponse editEntry(User user, Long entryId, EntryEditRequest request) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested entry (id: " + entryId + ") not found."));

        entryMapper.updateFromRequest(entry, request);
        return entryMapper.generateResponse(entry);
    }

    @Transactional
    public List<EntryResponse> reorderEntries(User user, EntriesReorderRequest request) {
        
        // Transform incoming reorder requests into a form that is searchable by entryId.
        Map<Long, EntryReorderRequest> requestsById = request.requests().stream().collect(Collectors.toMap(
                EntryReorderRequest::entryId,
                r -> r,
                (a, b) -> {throw new IllegalDuplicateValueException("Received multiple positions for the same entry.");}
        ));

        List<Entry> entries = entryRepository.findMultipleByIdVerified(user.getId(), requestsById.keySet());
        if (requestsById.size() > entries.size()) {
                throw new ResourceNotFoundException("One or more of the requested entries was not found.");
        }

        entries.forEach(entry ->
                entryMapper.repositionEntry(entry, requestsById.get(entry.getId())));

        return entries.stream().map(entryMapper::generateResponse).toList();
    }

    @Transactional
    public void deleteEntries(User user, EntryBulkRequest request) {
        Set<Long> entryIds = request.entryIds();
        Long count = entryRepository.multipleIdsExistVerified(user.getId(), entryIds);
        if (entryIds.size() > count) {
                throw new ResourceNotFoundException("One or more of the requested entries were not found.");
        }
        entryRepository.deleteMultipleByIdVerified(user.getId(), entryIds);
    }

    public EntryResponse retrieveEntry(User user, Long entryId) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested entry (id: " + entryId + ") not found.")
                );
        return entryMapper.generateResponse(entry);
    }

}