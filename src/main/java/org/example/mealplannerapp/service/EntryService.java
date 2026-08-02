package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.entry.request.EntryDuplicateRequest;
import org.example.mealplannerapp.dto.entry.request.EntryMoveRequest;
import org.example.mealplannerapp.dto.entry.request.create.EntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ServiceValidationException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.projection.Placement;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that handles create, duplicate, edit, move, delete, and retrieve
 * operations on individual {@link Entry} entities.
 */
@Service
@AllArgsConstructor
public class EntryService {

    // TODO: Javadocs for all.

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

    // TODO: Transactional or not?
    public EntryResponse duplicateEntry(User user, Long dayId, EntryDuplicateRequest request) {
        Entry entry = entryRepository.fetchByIdVerified(user.getId(), request.entryId())
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + request.entryId() + ") not found."));

        Day day = dayRepository.fetchByIdVerified(user.getId(), dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + dayId + ") not found."));

        Entry copy = entry.createDuplicate();
        copy.setDay(day);
        copy.setCategory(request.category());

        int count = entryRepository.countByDayAndCategory(dayId, request.category());
        copy.setPosition(count + 1);

        Entry saved = entryRepository.save(copy);
        return entryMapper.generateResponse(saved);
    }

    @Transactional
    public EntryResponse editEntry(User user, Long entryId, EntryEditRequest request) {
        Entry entry = entryRepository.fetchByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));

        entryMapper.updateFromRequest(entry, request);
        entry.snapshotNutritionAndPriceInfo();

        return entryMapper.generateResponse(entry);
    }

    @Transactional
    public void moveEntry(User user, Long entryId, EntryMoveRequest request) {
        Entry entry = entryRepository.fetchByIdVerified(user.getId(), entryId)
            .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));

        Long dayId = entry.getDay().getId();
        int currentPosition = entry.getPosition();
        int desiredPosition = request.desiredPosition();

        // TODO: Might reorder some of this code?
        if (entry.getCategory() == request.category() && currentPosition == desiredPosition) {
            throw new ServiceValidationException("Requested entry is already in the desired category and position.");
        }

        int count = entryRepository.countByDayAndCategory(dayId, request.category());

        if (entry.getCategory() == request.category()) {
            if (desiredPosition > count) {
                throw new ServiceValidationException("Desired position is out of bounds.");
            }

            if (currentPosition > desiredPosition) {
                entryRepository.shiftUpByDayAndCategory(dayId, request.category(), desiredPosition, currentPosition);
            } else if (currentPosition < desiredPosition) {
                entryRepository.shiftDownByDayAndCategory(dayId, request.category(), currentPosition, desiredPosition);
            }

            entry.setPosition(desiredPosition);

        } else {
            if (desiredPosition > count + 1) {
                throw new ServiceValidationException("Desired position is out of bounds.");
            }

            entryRepository.shiftUpByDayAndCategory(dayId, request.category(), desiredPosition, null);
            entryRepository.shiftDownByDayAndCategory(dayId, entry.getCategory(), currentPosition, null);

            entry.setCategory(request.category());
            entry.setPosition(desiredPosition);
        }
    }

    @Transactional
    public void deleteEntry(User user, Long entryId) {
        Placement placement = entryRepository.extractPlacementByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));

        entryRepository.deleteByIdVerified(user.getId(), entryId);

        entryRepository.shiftDownByDayAndCategory(
                placement.getDayId(),
                placement.getCategory(),
                placement.getPosition(),
                null);
    }

    public EntryResponse retrieveEntry(User user, Long entryId) {
        Entry entry = entryRepository.fetchByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));

        return entryMapper.generateResponse(entry);
    }

}
