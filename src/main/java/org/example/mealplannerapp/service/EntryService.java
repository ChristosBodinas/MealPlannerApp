package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.entry.request.EntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final EntryMapper entryMapper;

    private final DayRepository dayRepository;
    private final FoodRepository foodRepository;

    // (POST) createEntry(User user, Long dayId, EntryCreateRequest request)
    public EntryResponse createEntryInDay(User user, Long dayId, EntryCreateRequest request) {
        Entry entry = entryMapper.createFromRequest(request);

        Day day = dayRepository.findByIdVerified(user.getId(), dayId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested day (id: " + dayId + ") not found.")
                );
        entry.setDay(day);

        switch (request) {
            case FoodEntryCreateRequest fRequest:
                Food food = foodRepository.findByIdVerified(user.getId(), fRequest.foodId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Requested food (id: " + fRequest.foodId() + ") not found.")
                        );
                ((FoodEntry) entry).setFood(food);
                break;
        }

        entry.snapshotNutritionAndPriceInfo();

        Entry saved = entryRepository.save(entry);
        return entryMapper.generateResponse(saved);
    }

    // (POST) duplicateEntries(User user, Long dayId, List<Long> entryIds)

    // (PATCH) editSelectedEntry

    // (PATCH) reorderEntries

    // (DELETE) deleteEntries

    // (GET) retrieveEntry
    public EntryResponse retrieveEntry(User user, Long entryId) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), entryId);
        return entryMapper.generateResponse(entry);
    }

}