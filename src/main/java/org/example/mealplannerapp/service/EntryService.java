package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.entry.request.EntryDuplicateRequest;
import org.example.mealplannerapp.dto.entry.request.EntryMoveRequest;
import org.example.mealplannerapp.dto.entry.request.create.EntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.example.mealplannerapp.repository.DayRepository;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

/**
 * Service that handles create, duplicate, edit, move, delete, and retrieve
 * operations on individual {@link Entry} entities.
 */
@Service
@AllArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final DayRepository dayRepository;
    private final FoodRepository foodRepository;
    private final EntryMapper entryMapper;

    // TODO: Transactional or not?
    public EntryResponse createEntry(User user, Long dayId, EntryCreateRequest request) {
        Day day = dayRepository.fetchByIdVerified(user.getId(), dayId)  // 
            .orElseThrow(() -> new ResourceNotFoundException("Requested day (id: " + dayId + ") not found."));

        Entry entry = entryMapper.createFromRequest(request);
        entry.setDay(day);

        int count = entryRepository.countByDayAndCategory(dayId, request.category());
        entry.setPosition(count + 1);

        switch (request) {
            case FoodEntryCreateRequest f:
                Food food = foodRepository.fetchByIdVerified(user.getId(), f.foodId())
                    .orElseThrow(() -> new ResourceNotFoundException("Requested food (id: " + f.foodId() + ") not found."));
                ((FoodEntry) entry).setFood(food);
                break;
        }

        entry.snapshotNutritionAndPriceInfo();

        Entry saved = entryRepository.save(entry);
        return entryMapper.generateResponse(saved);
    }




/*

    @Transactional
    public void deleteEntry(User user, Long entryId) {
        PositionData positionData = entryRepository.findPositionDataByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));

        // Check only exists to catch race conditions. Might remove later.
        entryRepository.deleteByIdVerified(user.getId(), entryId);

        entryRepository.shiftDownInDayAndCategory(
                positionData.getDayId(), positionData.getCategory(), positionData.getPosition(), null
        );
    }

    @Transactional
    public EntryResponse duplicateEntry(User user, Long dayId, EntryDuplicateRequest request) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), request.entryId())
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + request.entryId() + ") not found."));

        Day day = dayRepository.findByIdVerified(user.getId(), dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested day (id: " + dayId + ") not found."));

        Entry copy = entry.createDuplicate();
        copy.setDay(day);
        copy.setCategory(request.category());

        long count = entryRepository.countInDayAndCategory(dayId, request.category());
        copy.setPosition(((int) count) + 1);

        Entry saved = entryRepository.save(copy);
        return entryMapper.generateResponse(saved);
    }

 
    @Transactional
    public EntryResponse editEntry(User user, Long entryId, EntryEditRequest request) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id:" + entryId + ") not found."));

        entryMapper.updateFromRequest(entry, request);
        entry.snapshotNutritionAndPriceInfo();  // Recalculate snapshot fields to account for the change.

        return entryMapper.generateResponse(entry);
    }


    @Transactional
    public void moveEntry(User user, Long dayId, Long entryId, EntryMoveRequest request) {
        Entry entry = entryRepository.findShallowByIdAndDayVerified(user.getId(), dayId, entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));

        int sourcePosition = entry.getPosition();
        int categoryCount = (int) entryRepository.countInDayAndCategory(dayId, request.category());
        int targetPosition;

        if (entry.getCategory() == request.category()) {
            targetPosition = Math.min(request.desiredPosition(), categoryCount);

            if (sourcePosition > targetPosition) {
                entryRepository.shiftUpInDayAndCategory(dayId, request.category(), targetPosition, sourcePosition);
            } else if (sourcePosition < targetPosition) {
                entryRepository.shiftDownInDayAndCategory(dayId, request.category(), sourcePosition, targetPosition);
            }
        } else {
            targetPosition = Math.min(request.desiredPosition(), categoryCount + 1);

            entryRepository.shiftUpInDayAndCategory(dayId, request.category(), null, targetPosition);
            entryRepository.shiftDownInDayAndCategory(dayId, entry.getCategory(), sourcePosition, null);

            entry.setCategory(request.category());
        }

        entry.setPosition(targetPosition);
    }

    public EntryResponse retrieveEntry(User user, Long entryId) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));
        return entryMapper.generateResponse(entry);
    }
    }
*/
    
}
